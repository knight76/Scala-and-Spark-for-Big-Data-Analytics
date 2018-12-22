package chapter11

import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.{SparkConf, SparkContext}

object RandomForestDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("PCAExample").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val data = MLUtils.loadLibSVMFile(sc, "data/mnist.bz2")

    val splits = data.randomSplit(Array(0.75, 0.25), seed = 12345L)
    val training = splits(0).cache()
    val test = splits(1)

    // 빈 categoricalFeaturesInfo를 사용해 랜덤 포레스트 모델을 트레이닝한다.
    // 모든 피쳐가 데이터셋에서 연속적이기 때문에 관련 작업이 필요하다.
    val numClasses = 10 //MNIST 데이터 셋의 클래스의 개수
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 50 // 실제 상황에서는 더 큰 수를 사용한다. 값이 더 클수록 좋다
    val featureSubsetStrategy = "auto" // 알고리즘을 선택한다
    val impurity = "gini" // 지니 계수
    val maxDepth = 30 // 실제 상황에서는 값이 더 클수록 좋다
    val maxBins = 32 // 실제 상황에서는 값이 더 클수록 좋다

    val model = RandomForest.trainClassifier(training, 
                                             numClasses, 
                                             categoricalFeaturesInfo, 
                                             numTrees, 
                                             featureSubsetStrategy, 
                                             impurity, 
                                             maxDepth, 
                                             maxBins)

    // 모델을 평가할 수 있도록 테스트 셋의 원 점수를 계산한다
    val labelAndPreds = test.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    
    val metrics = new MulticlassMetrics(labelAndPreds)

    // 혼동 행렬
    println("Confusion matrix:")
    println(metrics.confusionMatrix)

    // 전체 통계
    val accuracy = metrics.accuracy
    println("Summary Statistics")
    println(s"Accuracy = $accuracy")

    // 레이블 당 정확도
    val labels = metrics.labels
    labels.foreach { l =>
      println(s"Precision($l) = " + metrics.precision(l))
    }

    // 레이블 당 회수율
    labels.foreach { l =>
      println(s"Recall($l) = " + metrics.recall(l))
    }

    // 레이블 당 거짓 긍정 비율
    labels.foreach { l =>
      println(s"FPR($l) = " + metrics.falsePositiveRate(l))
    }

    // 레이블 당 F-측정 값
    labels.foreach { l =>
      println(s"F1-Score($l) = " + metrics.fMeasure(l))
    }

    // 가중 통계
    println(s"Weighted precision: ${metrics.weightedPrecision}")
    println(s"Weighted recall: ${metrics.weightedRecall}")
    println(s"Weighted F1 score: ${metrics.weightedFMeasure}")
    println(s"Weighted false positive rate: ${metrics.weightedFalsePositiveRate}")
    val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / test.count()
    println("Accuracy = " + (1-testErr) * 100 + " %")
    //println("Learned classification forest model:\n" + model.toDebugString)
  }
}