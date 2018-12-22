package chapter14

import org.apache.spark.mllib.clustering.GaussianMixture
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._

object GaussianMixtureModelDemo {
  case class Land(Price: Double, LotSize: Double, Waterfront: Double, Age: Double, LandValue: Double, NewConstruct: Double, CentralAir: Double, FuelType: Double, HeatType: Double, SewerType: Double, LivingArea: Double, PctCollege: Double, Bedrooms: Double, Fireplaces: Double, Bathrooms: Double, rooms: Double)

  // Array[Double] 타입의 RDD를 Land로 변환하는 메소드
  def parseLand(line: Array[Double]): Land = {
    Land(
      line(0), line(1), line(2), line(3), line(4), line(5),
      line(6), line(7), line(8), line(9), line(10),
      line(11), line(12), line(13), line(14), line(15))
  }

  // String 타입의 RDD를 Array[Double] 타입의 RDD로 변환하는 메소드
  def parseRDD(rdd: RDD[String]): RDD[Array[Double]] = {
    rdd.map(_.split(",")).map(_.map(_.toDouble))
  }

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName("KMeans")
      .getOrCreate()

    import spark.sqlContext.implicits._

    // 데이터셋을 파싱한다.
    val start = System.currentTimeMillis()
    val landDF = parseRDD(spark.sparkContext.textFile("data/Saratoga_NY_Homes.txt")).map(parseLand).toDF().cache()

    // 데이터 프레임(즉 landDF)을 Double 타입의 RDD로 변환하고 클러스터 번호를 연결하는 새로운 데이터 프레임을 생성할 수 있는 데이터를 캐시한다
    val rowsRDD = landDF.rdd.map(r => (r.getDouble(0), r.getDouble(1), r.getDouble(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getDouble(9), r.getDouble(10), r.getDouble(11), r.getDouble(12), r.getDouble(13), r.getDouble(14), r.getDouble(15)))
    rowsRDD.cache()

    // K 평균 모델을 트레이닝하기 위해 rdd로 변환하고 캐싱한다
    val landRDD = landDF.rdd.map(r => Vectors.dense(r.getDouble(1), r.getDouble(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getDouble(9), r.getDouble(10), r.getDouble(11), r.getDouble(12), r.getDouble(13), r.getDouble(14), r.getDouble(15)))
    landRDD.cache()

    // GaussianMixture를 사용해 데이터를 클러스터링한다.
    val K = 5
    val maxIteration = 20
    val model = new GaussianMixture()
                .setK(K)  // 원하는 클러스터 개수
                .setMaxIterations(maxIteration) // 최대 횟수
                .setConvergenceTol(0.05)    // 수렴 허용 오차
                .setSeed(12345)  // 램덤을 허용하지 않게 시드를 설정한다.
                .run(landRDD)    // 트레이닝 셋을 사용한 모델을 피팅한다.

    // ID를 가진 모델에서 예측을 얻어 다른 정보로 다시 연결할 수 있다.
    val predictions = rowsRDD.map{r => (r._1, model.predict(Vectors.dense(r._2, r._3, r._4, r._5, r._6, r._7, r._8, r._9, r._10, r._11, r._12, r._13, r._14, r._15, r._16) ))}

    // rdd를 데이터 프레임으로 변환한다
    val predCluster = predictions.toDF("Price", "CLUSTER")
    predCluster.show()

    // 각 주택에 대해 개별 클러스터 번호를 알기 위해 예측 데이터 프레임과 기존 데이터 프레임을 조인한다.
    val newDF = landDF.join(predCluster, "Price")
    newDF.show()

    // 각 클러스터의 하위 집합을 살펴본다.
    newDF.filter("CLUSTER = 0").show()
    newDF.filter("CLUSTER = 1").show()
    newDF.filter("CLUSTER = 2").show()
    newDF.filter("CLUSTER = 3").show()
    newDF.filter("CLUSTER = 4").show()

    // 각 클러스터의 기술 통계 정보를 살펴본다.
    newDF.filter("CLUSTER = 0").describe().show()
    newDF.filter("CLUSTER = 1").describe().show()
    newDF.filter("CLUSTER = 2").describe().show()
    newDF.filter("CLUSTER = 3").describe().show()
    newDF.filter("CLUSTER = 4").describe().show()
    println("Cluster ID: ")
    val pred = model.predict(landRDD).foreach {println}

    val end = System.currentTimeMillis()
    println("Model building and prediction time: "+ {end - start} + "ms")

    // 최댓값 가능도 모델의 결과 파라미터
    for (i <- 0 until model.k) {
      println("weight=%f\nmu=%s\nsigma=\n%s\n" format(model.weights(i), model.gaussians(i).mu, model.gaussians(i).sigma))
    }
    spark.stop()
  }
}