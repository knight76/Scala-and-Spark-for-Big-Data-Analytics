package chapter12

import edu.stanford.nlp.process.Morphology
import edu.stanford.nlp.simple.Document
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.{Vector => MLVector}
import org.apache.spark.mllib.clustering.{DistributedLDAModel, EMLDAOptimizer, LDA, OnlineLDAOptimizer}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.JavaConversions._

object TopicModellingwithLDA {
  def main(args: Array[String]): Unit = {
    val lda = new LDAforTM() // 실제 계산은 여기서 수행된다
    val defaultParams = Params().copy(input = "data/docs/") // LDA 모델을 트레이닝하기 위해 매개 변수를 로드한다
    lda.run(defaultParams) // 기본 매개 변수로 LDA 모델을 트레이닝한다

  }
}

// LDA 모델을 트레이닝하기 전에 매개 변수를 설정한다
case class Params(input: String = "",
                  k: Int = 5,
                  maxIterations: Int = 20,
                  docConcentration: Double = -1,
                  topicConcentration: Double = -1,
                  vocabSize: Int = 2900000,
                  stopwordFile: String = "data/docs/stopWords.txt",
                  algorithm: String = "em",
                  checkpointDir: Option[String] = None,
                  checkpointInterval: Int = 10)

// 주제 모델링에 대한 실제 계산은 여기서 이루어진다
class LDAforTM() {
  val spark = SparkSession
    .builder
    .master("local[*]")
    .appName("LDA for topic modelling")
    .getOrCreate()

  def run(params: Params): Unit = {
    Logger.getRootLogger.setLevel(Level.WARN)

    // LDA 모델에서 다큐먼트를 로드하고 준비한다
    val preprocessStart = System.nanoTime()
    val (corpus, vocabArray, actualNumTokens) = preprocess(params.input, params.vocabSize, params.stopwordFile)

    val actualCorpusSize = corpus.count()
    val actualVocabSize = vocabArray.length
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
    corpus.cache() //will be reused later steps

    println()
    println("Training corpus summary:")
    println("-------------------------------")
    println("Training set size: " + actualCorpusSize + " documents")
    println("Vocabulary size: " + actualVocabSize + " terms")
    println("Number of tockens: " + actualNumTokens + " tokens")
    println("Preprocessing time: " + preprocessElapsed + " sec")
    println("-------------------------------")
    println()

    // LDA 모델 인스턴스를 생성한다
    val lda = new LDA()

    val optimizer = params.algorithm.toLowerCase match {
      case "em" => new EMLDAOptimizer
      // 작은 데이터셋이 더 강력해질 수 있도록 MiniBatchFraction에 (1.0/actualCorpusSize)를 추가한다
      case "online" => new OnlineLDAOptimizer().setMiniBatchFraction(0.05 + 1.0 / actualCorpusSize)
      case _ => throw new IllegalArgumentException("Only em, online are supported but got ${params.algorithm}.")
    }

    lda.setOptimizer(optimizer)
      .setK(params.k)
      .setMaxIterations(params.maxIterations)
      .setDocConcentration(params.docConcentration)
      .setTopicConcentration(params.topicConcentration)
      .setCheckpointInterval(params.checkpointInterval)

    if (params.checkpointDir.nonEmpty) {
      spark.sparkContext.setCheckpointDir(params.checkpointDir.get)
    }

    val startTime = System.nanoTime()

    // 트레이닝한 문서 컬렉션을 사용해 LDA 모델을 트레이닝기 시작한다
    val ldaModel = lda.run(corpus)
    val elapsed = (System.nanoTime() - startTime) / 1e9

    println("Finished training LDA model.  Summary:")
    println("Training time: " + elapsed + " sec")

    if (ldaModel.isInstanceOf[DistributedLDAModel]) {
      val distLDAModel = ldaModel.asInstanceOf[DistributedLDAModel]
      val avgLogLikelihood = distLDAModel.logLikelihood / actualCorpusSize.toDouble
      println("The average log likelihood of the training data: " +  avgLogLikelihood)
      println()
    }

    // 각 주제에 대한 가중치가 높은 단어를 보여주며 주제를 출력한다
    val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = 10)
    println(topicIndices.length)
    val topics = topicIndices.map {case (terms, termWeights) => terms.zip(termWeights).map { case (term, weight) => (vocabArray(term.toInt), weight) } }

    var sum = 0.0
    println(s"${params.k} topics:")
    topics.zipWithIndex.foreach {
      case (topic, i) =>
        println(s"TOPIC $i")
        println("------------------------------")
        topic.foreach {
          case (term, weight) =>
            term.replaceAll("\\s", "")
            println(s"$term\t$weight")
            sum = sum + weight
        }
        println("----------------------------")
        println("weight: " + sum)
        println()
    }
    spark.stop()
  }

  // 원본 텍스트에 대한 전처리를 수행한다
  def preprocess(paths: String, vocabSize: Int, stopwordFile: String): (RDD[(Long, Vector)], Array[String], Long) = {
    import spark.implicits._
    // 전체 텍스트 파일을 읽는다
    val initialrdd = spark.sparkContext.wholeTextFiles(paths).map(_._2)
    initialrdd.cache()

    val rdd = initialrdd.mapPartitions { partition =>
      val morphology = new Morphology()
      partition.map { value => helperForLDA.getLemmaText(value, morphology) }
    }.map(helperForLDA.filterSpecialCharacters)

    rdd.cache()
    initialrdd.unpersist()
    val df = rdd.toDF("docs")
    df.show()

    // 정지 단어를 사용자 정의한다
    val customizedStopWords: Array[String] = if (stopwordFile.isEmpty) {
      Array.empty[String]
    } else {
      val stopWordText = spark.sparkContext.textFile(stopwordFile).collect()
      stopWordText.flatMap(_.stripMargin.split(","))
    }

    // RegexTokenizer를 사용해 토큰을 생성한다
    val tokenizer = new RegexTokenizer().setInputCol("docs").setOutputCol("rawTokens")

    // StopWordsRemover를 사용해 정지 단어를 제거한다
    val stopWordsRemover = new StopWordsRemover().setInputCol("rawTokens").setOutputCol("tokens")
    stopWordsRemover.setStopWords(stopWordsRemover.getStopWords ++ customizedStopWords)

    //토큰을 CountVector로 변환한다
    val countVectorizer = new CountVectorizer().setVocabSize(vocabSize).setInputCol("tokens").setOutputCol("features")

    // 파이프 라인을 설정한다
    val pipeline = new Pipeline().setStages(Array(tokenizer, stopWordsRemover, countVectorizer))

    val model = pipeline.fit(df)
    val documents = model.transform(df).select("features").rdd.map {
      case Row(features: MLVector) => Vectors.fromML(features)
    }.zipWithIndex().map(_.swap)

    // 단어와 토큰 개수 짝을 리턴한다
    (documents, model.stages(2).asInstanceOf[CountVectorizerModel].vocabulary, documents.map(_._2.numActives).sum().toLong)
  }
}

object helperForLDA {
  def filterSpecialCharacters(document: String) = document.replaceAll("""[! @ # $ % ^ & * ( ) _ + - − , " ' ; : . ` ? --]""", " ")

  def getLemmaText(document: String, morphology: Morphology) = {
    val string = new StringBuilder()
    val value = new Document(document).sentences().toList.flatMap { a =>
      val words = a.words().toList
      val tags = a.posTags().toList
      (words zip tags).toMap.map { a =>
        val newWord = morphology.lemma(a._1, a._2)
        val addedWoed = if (newWord.length > 3) {
          newWord
        } else { "" }
        string.append(addedWoed + " ")
      }
    }
    string.toString()
  }
}