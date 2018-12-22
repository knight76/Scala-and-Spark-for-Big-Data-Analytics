package chapter13

import java.io._

import org.apache.spark.ml.classification.{LogisticRegression, OneVsRest}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.SparkSession

object OneVsRestExample {
  def main(args: Array[String]): Unit = {

    // 마스터 URL을 명세함으로서 스파크 세션을 생성한다
    val spark = SparkSession
      .builder
      .master("local[*]") //적절히 업데이트한다
      .appName(s"OneVsRestExample")
      .getOrCreate()

    val path = args(0)

    // 파일에서 데이터를 로드하고 데이터를 데이터 프레임으로 생성한다
    val inputData = spark.read.format("libsvm").load("data/Letterdata_libsvm.data")
    //inputData.show()

    // 모델을 트레이닝하기 위해 트레이닝 셋(70%)과 테스트 셋(30%)을 생성한다
    val Array(train, test) = inputData.randomSplit(Array(0.7, 0.3))

    // 기본 분류자(로지스틱 회귀 분류자)를 초기화한다
    val classifier = new LogisticRegression()
      .setMaxIter(500) // 최대 반복 횟수를 지정한다. 일반적으로 더 많을수록 좋다.
      .setTol(1E-4) // 정지 기준에 대한 허용 오차다. 일반적으로 모델을 더 집중적으로 트레이닝할 때 도움이 되며, 작은 값일수록 좋다. 기본 값은 1E-4이다.
      .setFitIntercept(true) // 결정 함수에 특정 상수(바이어스 또는 인터셉트)가 추가될 수 있는지 지정한다.
      .setStandardization(true) // 부울 값
      .setAggregationDepth(50) // 많을 수록 좋다
      .setRegParam(0.0001) // 적을 수록 좋다
      .setElasticNetParam(0.01) // 대부분 적을 수록 좋다

    // OVTR 분류자를 인스턴스로 생성한다
    val ovr = new OneVsRest().setClassifier(classifier)

    // 다중 클래스 모델을 트레이닝한다
    val ovrModel = ovr.fit(train)

    // 테스트 셋에서 모델의 점수를 매긴다
    val predictions = ovrModel.transform(test)

    // 모델을 평가하고 정확도, 정밀도, 회수율, f1 측정과 같은 분류 성능 메트릭을 계산한다
    val evaluator1 = new MulticlassClassificationEvaluator().setLabelCol("label").setPredictionCol("prediction").setMetricName("accuracy")
    val evaluator2 = new MulticlassClassificationEvaluator().setLabelCol("label").setPredictionCol("prediction").setMetricName("weightedPrecision")
    val evaluator3 = new MulticlassClassificationEvaluator().setLabelCol("label").setPredictionCol("prediction").setMetricName("weightedRecall")
    val evaluator4 = new MulticlassClassificationEvaluator().setLabelCol("label").setPredictionCol("prediction").setMetricName("f1")

    val accuracy = evaluator1.evaluate(predictions)
    val precision = evaluator2.evaluate(predictions)
    val recall = evaluator3.evaluate(predictions)
    val f1 = evaluator4.evaluate(predictions)


    val writer = new PrintWriter(new File("~/output.txt" ))
    writer.write(s"Accuracy: "+ accuracy + "Precision:  " + precision)
    //writer.write("Prediction Matrix"+ result)
    writer.close()

    // 성능 메트릭을 출력한다
    println("Accuracy = " + accuracy);
    println("Precision = " + precision)
    println("Recall = " + recall)
    println("F1 = " + f1)
    println(s"Test Error = ${1 - accuracy}")

    //inputData.show()
    predictions.show(false)

    // 스파크 세션을 종료한다
    spark.stop()
  }
}
