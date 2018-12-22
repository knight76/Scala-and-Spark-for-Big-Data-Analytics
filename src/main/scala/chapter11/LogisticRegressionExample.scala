package chapter11

import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.SparkSession

object SVMWithSGDExample {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName("LogisticRegressionExample")
      .getOrCreate()

    // LIBSVM 포맷으로 트레이닝 데이터를 로드한다
    val data = MLUtils.loadLibSVMFile(spark.sparkContext, "data/mnist.bz2")

    // 데이터를 트레이닝(75%)과 테스트(25%)로 나눈다
    val splits = data.randomSplit(Array(0.75, 0.25), seed = 12345L)
    val training = splits(0).cache()
    val test = splits(1)
    
    // 모델을 구축하기 위해 트레이닝 알고리즘을 실행한다
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(10)
      .setIntercept(true)
      .setValidateData(true)
      .run(training)

    // default threshold를 정리한다
    model.clearThreshold()

    // 테스트 셋에 대한 원 점수를 계산한다
    val scoreAndLabels = test.map { point =>
      val score = model.predict(point.features)
      (score, point.label)
    }

    val metrics = new MulticlassMetrics(scoreAndLabels)

    // 혼동 행렬
    println("Confusion matrix:")
    println(metrics.confusionMatrix)

    // 전체적인 통계
    val accuracy = metrics.accuracy
    println("Summary Statistics")
    println(s"Accuracy = $accuracy")

    // 레이블 별 정확도
    val labels = metrics.labels
    labels.foreach { l =>
      println(s"Precision($l) = " + metrics.precision(l))
    }

    // 레이블 별 재현율
    labels.foreach { l =>
      println(s"Recall($l) = " + metrics.recall(l))
    }

    // 레이블 별 거짓 긍정 비율
    labels.foreach { l =>
      println(s"FPR($l) = " + metrics.falsePositiveRate(l))
    }

    // 레이블 별 F-측정
    labels.foreach { l =>
      println(s"F1-Score($l) = " + metrics.fMeasure(l))
    }
    
    // 가중 통계
    println(s"Weighted precision: ${metrics.weightedPrecision}")
    println(s"Weighted recall: ${metrics.weightedRecall}")
    println(s"Weighted F1 score: ${metrics.weightedFMeasure}")
    println(s"Weighted false positive rate: ${metrics.weightedFalsePositiveRate}")

    spark.stop()
  }
}
