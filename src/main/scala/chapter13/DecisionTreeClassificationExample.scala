package chapter13

import org.apache.spark.ml.Pipeline // 파이프라인 생성을 위해 임포트한다
import org.apache.spark.ml.classification.DecisionTreeClassificationModel
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}
import org.apache.spark.sql.SparkSession //스파크 세션을 생성하기 위해 임포트한다.

object DecisionTreeClassificationExample {
  def main(args: Array[String]): Unit = {
    // 스파크 세션을 생성한다
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName(s"DecisionTreeClassifier")
      .getOrCreate()

    // LIBSVM 포맷으로 저장된 데이터를 데이터 프레임으로 로드한다.
    val data = spark.read.format("libsvm").load("data/Letterdata_libsvm.data")

    // label 컬럼을 인덱싱한다 - label 칼럼에 메타데이터를 추가하고 인덱싱한다.
    // 그다음 인덱싱된 컬럼(indexedLabel)의 모든 레이블을 포함할 수 있게 모든 데이터셋에 피팅한다.
    val labelIndexer = new StringIndexer()
      .setInputCol("label")
      .setOutputCol("indexedLabel")
      .fit(data)

    // 범주형 features를 식별하고 인덱싱한다.
    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4)
      .fit(data)

    // 데이터를 트레이닝 셋과 테스트 셋으로 구분한다(25%는 테스트용).
    val Array(trainingData, testData) = data.randomSplit(Array(0.75, 0.25), 12345L)

    // 결정 트리 모델을 트레이닝한다.
    val dt = new DecisionTreeClassifier()
      .setLabelCol("indexedLabel")
      .setFeaturesCol("indexedFeatures")

    // 인덱스된 레이블을 원래 레이블로 다시 변환한다.
    val labelConverter = new IndexToString()
      .setInputCol("prediction")
      .setOutputCol("predictedLabel")
      .setLabels(labelIndexer.labels)

    // 인덱서, 레이블 트랜스포머, 트리를 함께 체이닝하여 결정 트리 파이프라인을 생성한다.
    val pipeline = new Pipeline()
      .setStages(Array(labelIndexer, featureIndexer, dt, labelConverter))

    // 트레이닝 모델. 인덱서가 적용된다.
    val model = pipeline.fit(trainingData)

    // 테스트 셋에 대한 예측을 생성하고, 일부 예를 출력한다
    val predictions = model.transform(testData)
    predictions.show()

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
    val evaluator1 = evaluator.setMetricName("accuracy")
    val evaluator2 = evaluator.setMetricName("weightedPrecision")
    val evaluator3 = evaluator.setMetricName("weightedRecall")
    val evaluator4 = evaluator.setMetricName("f1")

    // 테스트 데이터에 대해 분류 정확도, 정확도, 재현율, f1 측정, 테스트 데이터의 오차를 계산한다
    val accuracy = evaluator1.evaluate(predictions)
    val precision = evaluator2.evaluate(predictions)
    val recall = evaluator3.evaluate(predictions)
    val f1 = evaluator4.evaluate(predictions)

    // 성능 메트릭을 출력한다.
    println("Accuracy = " + accuracy);
    println("Precision = " + precision)
    println("Recall = " + recall)
    println("F1 = " + f1)
    println(s"Test Error = ${1 - accuracy}")

    val treeModel = model.stages(2).asInstanceOf[DecisionTreeClassificationModel]
    println("Learned classification tree model:\n" + treeModel.toDebugString)

    spark.stop()
  }
}