package com.example.ZeepleinAndSpark

import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature._
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}

object SpamFilteringDemo {
  val customSchema = StructType(Array(
    StructField("RawLabel", StringType, true),
    StructField("SmsText", StringType, true)))

  def main(args: Array[String]) {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("SpamFilteringDemo")
      .master("local[*]")
      .getOrCreate();

    val data = spark.read.format("com.databricks.spark.csv").option("inferSchema", "false").option("delimiter", "\t").schema(customSchema).load("data/SMSSpamCollection")
    data.show()

    import org.apache.spark.sql.functions.count
    import spark.implicits._

    // StringIndexer를 생성한다
    val indexer = new StringIndexer()
      .setInputCol("RawLabel")
      .setOutputCol("label")
    val indexed = indexer.fit(data).transform(data)
    indexed.show()

    val regexTokenizer = new RegexTokenizer()
      .setInputCol("SmsText")
      .setOutputCol("words")
      .setPattern("\\W+")
      .setGaps(true)

    //val countTokens = udf { (words: Seq[String]) => words.length }
    val regexTokenized = regexTokenizer.transform(indexed)

    val remover = new StopWordsRemover()
      .setInputCol("words")
      .setOutputCol("filtered")

    val tokenDF = remover.transform(regexTokenized)
    tokenDF.show()

    val spamTokensWithFrequenciesDF = tokenDF
      .filter($"label" === 1.0)
      .select($"filtered")
      .flatMap(row => row.getAs[Seq[String]](0))
      .filter(word => (word.length() > 1))
      .toDF("Tokens")
      .groupBy($"Tokens")
      .agg(count("*").as("Frequency"))
      .orderBy($"Frequency".desc)

    spamTokensWithFrequenciesDF.createOrReplaceTempView("spamTokensWithFrequenciesDF")
    spamTokensWithFrequenciesDF.show()
    
    val hamTokensWithFrequenciesDF = tokenDF
      .filter($"label" === 0.0)
      .select($"filtered")
      .flatMap(row => row.getAs[Seq[String]](0))
      .filter(word => (word.length() > 1))
      .toDF("Tokens")
      .groupBy($"Tokens")
      .agg(count("*").as("Frequency"))
      .orderBy($"Frequency".desc)      

    hamTokensWithFrequenciesDF.createOrReplaceTempView("hamTokensWithFrequenciesDF")
    //spark.sql("select * from hamTokensWithFrequenciesDF order by Frequency desc limit 20 ").show()
    hamTokensWithFrequenciesDF.show()
    
    
    // ML를 적용한다
    
    // HashingTF를 생성한다
    val hashingTF = new HashingTF()
      .setInputCol("filtered").setOutputCol("tf")//.setNumFeatures(20)
    val tfdata = hashingTF.transform(tokenDF)

    tfdata.show()

    // IDF를 생성한
    val idf = new IDF().setInputCol("tf").setOutputCol("idf")
    val idfModel = idf.fit(tfdata)
    val idfdata = idfModel.transform(tfdata)
    idfdata.show()

    // VectorAssembler를 생성한다
    val assembler = new VectorAssembler()
      .setInputCols(Array("tf", "idf"))
      .setOutputCol("features")
    val assemDF = assembler.transform(idfdata)
    assemDF.show()
    
    // ML 모델을 트레이닝하기 위해 데이터 프레임을 준비한다
    val mlDF = assemDF.select("label", "features")

    // 트레이닝 데이터와 테스트 데이터를 분리한다
    val Array(trainingData, testData) = mlDF.randomSplit(Array(0.75, 0.25), 12345L)

    // LogisticRegression를 생성한다
    val lr = new LogisticRegression()
      .setLabelCol("label")
      .setFeaturesCol("features")
      .setRegParam(0.0001)
      .setElasticNetParam(0.0001)
      .setMaxIter(200)

    // 모델을 빌드한다
    val lrModel = lr.fit(trainingData)
    
    // 예측한다
    val predict = lrModel.transform(testData)
    predict.show(100)

    // 평가한다
    val evaluator = new BinaryClassificationEvaluator()
      .setRawPredictionCol("prediction")

    val accuracy = evaluator.evaluate(predict)
    println("Accuracy = " + accuracy*100 + "%")

    // 혼동 매트릭스를 계산한다
    val predictionsAndLabels = predict.select("prediction", "label")
      .map(row => (row.getDouble(0), row.getDouble(1)))

    val metrics = new MulticlassMetrics(predictionsAndLabels.rdd)
    println("\nConfusion matrix:")
    println(metrics.confusionMatrix)
  }
}

