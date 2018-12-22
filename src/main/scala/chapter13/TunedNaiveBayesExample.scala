package chapter13

import org.apache.spark.ml.classification.NaiveBayes
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}

object TunedNaiveBayesExample {
  def main(args: Array[String]): Unit = {
    // 스파크 세션을 생성한다
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName("Tuned NaiveBayes")
      .getOrCreate()

    // LIBSVM 포맷으로 저장된 데이터를 데이터 프레임으로 로드한다.
    val data = spark.read.format("libsvm").load("hdfs://data/webspam_wc_normalized_trigram.svm")

    // 데이터를 트레이닝 셋과 테스트 셋으로 구분한다(25%는 테스트용).
    val Array(trainingData, testData) = data.randomSplit(Array(0.75, 0.25), seed = 12345L)

    // 트레이닝 셋을 사용해 나이브 베이즈 모델을 트레이닝한다.
    val nb = new NaiveBayes().setSmoothing(0.00001)
    val model = nb.fit(trainingData)

    // 테스트 셋에 대한 예측을 생성하고, 일부 예를 출력한다
    val predictions = model.transform(testData)
    predictions.show()

    // 메트릭을 계산한다.
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
    val evaluator1 = evaluator.setMetricName("accuracy")
    val evaluator2 = evaluator.setMetricName("weightedPrecision")
    val evaluator3 = evaluator.setMetricName("weightedRecall")
    val evaluator4 = evaluator.setMetricName("f1")

    // 성능 메트릭을 계산한다.
    val accuracy = evaluator1.evaluate(predictions)
    val precision = evaluator2.evaluate(predictions)
    val recall = evaluator3.evaluate(predictions)
    val f1 = evaluator4.evaluate(predictions)

    // 성능 메트릭을 출력한다.
    println("Accuracy = " + accuracy)
    println("Precision = " + precision)
    println("Recall = " + recall)
    println("F1 = " + f1)
    println(s"Test Error = ${1 - accuracy}")

    spark.stop()
  }
}
