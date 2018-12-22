package chapter11

import org.apache.log4j.{Level, LogManager}
import org.apache.spark._
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.ml.{Pipeline, PipelineStage}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext

object CreditRiskAnalysisTuning {
  case class Credit(
    creditability: Double,
    balance: Double, duration: Double, history: Double, purpose: Double, amount: Double,
    savings: Double, employment: Double, instPercent: Double, sexMarried: Double, guarantors: Double,
    residenceDuration: Double, assets: Double, age: Double, concCredit: Double, apartment: Double,
    credits: Double, occupation: Double, dependents: Double, hasPhone: Double, foreign: Double)

  def parseCredit(line: Array[Double]): Credit = {
    Credit(
      line(0),
      line(1) - 1, line(2), line(3), line(4), line(5),
      line(6) - 1, line(7) - 1, line(8), line(9) - 1, line(10) - 1,
      line(11) - 1, line(12) - 1, line(13), line(14) - 1, line(15) - 1,
      line(16) - 1, line(17) - 1, line(18) - 1, line(19) - 1, line(20) - 1)
  }

  def parseRDD(rdd: RDD[String]): RDD[Array[Double]] = {
    rdd.map(_.split(",")).map(_.map(_.toDouble))
  }

  def main(args: Array[String]) {
    val log = LogManager.getRootLogger
    log.setLevel(Level.WARN)

    log.warn("Credit risk applicaiton started")

    val conf = new SparkConf().setAppName("CredictRiskPredicttion").setMaster("local[*]")
    val sc = new SparkContext(conf)
    
    // 데이터 셋을 RDD로 로드하고 파싱한다
    val creditRDD = parseRDD(sc.textFile("data/germancredit.csv")).map(parseCredit)
    
    // ML 파이프라인으로 데이터 프레임을 얻는다
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._
    val creditDF = creditRDD.toDF().cache()
    creditDF.createOrReplaceTempView("credit")
    creditDF.printSchema
    creditDF.show

    sqlContext.sql("SELECT creditability, avg(balance) as avgbalance, avg(amount) as avgamt, avg(duration) as avgdur  FROM credit GROUP BY creditability ").show
    creditDF.describe("balance").show
    creditDF.groupBy("creditability").avg("balance").show

    val featureCols = Array("balance", "duration", "history", "purpose", "amount",
      "savings", "employment", "instPercent", "sexMarried", "guarantors",
      "residenceDuration", "assets", "age", "concCredit", "apartment",
      "credits", "occupation", "dependents", "hasPhone", "foreign")

    val assembler = new VectorAssembler().setInputCols(featureCols).setOutputCol("features")
    val df2 = assembler.transform(creditDF)
    df2.select("features").show()

    val labelIndexer = new StringIndexer().setInputCol("creditability").setOutputCol("label")
    val df3 = labelIndexer.fit(df2).transform(df2)
    df3.select("label", "features").show

    val splitSeed = 5043
    val Array(trainingData, testData) = df3.randomSplit(Array(0.80, 0.20), splitSeed)

    val classifier = new RandomForestClassifier()
      .setImpurity("gini")
      .setMaxDepth(30)
      .setNumTrees(30)
      .setFeatureSubsetStrategy("auto")
      .setSeed(1234567)
      .setMaxBins(40)
      .setMinInfoGain(0.001)

    val model = classifier.fit(trainingData)
    val predictions = model.transform(testData)
    predictions.show()
    //model.toDebugString
        
   //////////////////////////////// 튜닝 전의 메트릭 ///////////////////////////////////////////////
    val binaryClassificationEvaluator = new BinaryClassificationEvaluator()
      .setLabelCol("label")
      .setRawPredictionCol("rawPrediction")

    val accuracy = binaryClassificationEvaluator.evaluate(predictions)
    println("The accuracy before pipeline fitting: " + accuracy)

     // Area Under Receiver Operating Characteristic (AUROC)과 Area Under Precision Recall Curve (AUPRC)을 계산한다
    def printlnMetric(metricName: String): Double = {
      val metrics = binaryClassificationEvaluator.setMetricName(metricName).evaluate(predictions)
      return metrics
    }
    
    println("Area Under ROC before tuning: " + printlnMetric("areaUnderROC"))        
    println("Area Under PRC before tuning: "+  printlnMetric("areaUnderPR"))
  
    val rm = new RegressionMetrics(
      predictions.select("prediction", "label").rdd.map(x =>
        (x(0).asInstanceOf[Double], x(1).asInstanceOf[Double])))

    println("MSE: " + rm.meanSquaredError)
    println("MAE: " + rm.meanAbsoluteError)
    println("RMSE Squared: " + rm.rootMeanSquaredError)
    println("R Squared: " + rm.r2)
    println("Explained Variance: " + rm.explainedVariance + "\n")
    ///////////////////////////////////////////////////////////////////////////////////////
    
    //////////////////////////////// 그리드 검색과 교차 검증을 튜닝한다 /////
    val paramGrid = new ParamGridBuilder()
      .addGrid(classifier.maxBins, Array(25, 30))
      .addGrid(classifier.maxDepth, Array(5, 10))
      .addGrid(classifier.numTrees, Array(20, 70))
      .addGrid(classifier.impurity, Array("entropy", "gini"))
      .build()

    val steps: Array[PipelineStage] = Array(classifier)
    val pipeline = new Pipeline().setStages(steps)

    val cv = new CrossValidator()
      .setEstimator(pipeline)
      .setEvaluator(binaryClassificationEvaluator)
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(10)

    val pipelineFittedModel = cv.fit(trainingData)
    val predictions2 = pipelineFittedModel.transform(testData)
    ////////////////////////////// 튜닝 완료 ///////////////////////////////////////
    
    //////////////////////////////// 튜닝 후 메트릭 ///////////////////////////////////////////////
    val accuracy2 = binaryClassificationEvaluator.evaluate(predictions2)
    println("The accuracy after pipeline fitting: " + accuracy2)
    
    // Area Under Receiver Operating Characteristic (AUROC)와 Area Under Precision Recall Curve (AUPRC)를 계산한다
    def printlnMetricAfter(metricName: String): Double = {
      val metrics = binaryClassificationEvaluator.setMetricName(metricName).evaluate(predictions2)
      return metrics
    }
    
    println("Area Under ROC after tuning: " + printlnMetricAfter("areaUnderROC"))        
    println("Area Under PRC after tuning: "+  printlnMetricAfter("areaUnderPR"))

    
    pipelineFittedModel
      .bestModel.asInstanceOf[org.apache.spark.ml.PipelineModel]
      .stages(0)
      .extractParamMap
      
    println("The best fitted model:" + pipelineFittedModel.bestModel.asInstanceOf[org.apache.spark.ml.PipelineModel].stages(0))
    //println()

    val rm2 = new RegressionMetrics(predictions2.select("prediction", "label").rdd.map(x => (x(0).asInstanceOf[Double], x(1).asInstanceOf[Double])))

    println("MSE: " + rm2.meanSquaredError)
    println("MAE: " + rm2.meanAbsoluteError)
    println("RMSE Squared: " + rm2.rootMeanSquaredError)
    println("R Squared: " + rm2.r2)
    println("Explained Variance: " + rm2.explainedVariance + "\n")
    log.warn("Credit risk applicaiton finshed")
    sc.stop()
  }
}