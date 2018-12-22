package chapter14

import org.apache.spark.mllib.clustering.BisectingKMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._

object BisectingKMeansDemo {
  // 모든 피처를 Double로 읽는 클래스
  case class Land(Price: Double, LotSize: Double, Waterfront: Double, Age: Double, LandValue: Double, NewConstruct: Double, CentralAir: Double, FuelType: Double, HeatType: Double, SewerType: Double, LivingArea: Double, PctCollege: Double, Bedrooms: Double, Fireplaces: Double, Bathrooms: Double, rooms: Double)

  // Array[Double] 타입의 RDD를 Land로 변환하는 메소드
  def parseLand(line: Array[Double]): Land = {
    Land(
      line(0), line(1), line(2), line(3), line(4), line(5),
      line(6), line(7), line(8), line(9), line(10),
      line(11), line(12), line(13), line(14), line(15)
    )
  }

  // function to transform an RDD of Strings into an RDD of Double
  def parseRDD(rdd: RDD[String]): RDD[Array[Double]] = {
    rdd.map(_.split(",")).map(_.map(_.toDouble))
  }

  def main(args: Array[String]): Unit =  {
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName("KMeans")
      .getOrCreate()

    import spark.sqlContext.implicits._

    // 데이터셋을 파싱한다.
    val start = System.currentTimeMillis()
    val dataPath = "data/Saratoga_NY_Homes.txt"
    val landDF = parseRDD(spark.sparkContext.textFile(dataPath)).map(parseLand).toDF().cache()
    landDF.show()

    // 데이터 프레임(즉 landDF)을 Double 타입의 RDD로 변환하고 클러스터 번호를 연결하는 새로운 데이터 프레임을 생성할 수 있는 데이터를 캐시한다.
    val rowsRDD = landDF.rdd.map(r => (r.getDouble(0), r.getDouble(1), r.getDouble(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getDouble(9), r.getDouble(10), r.getDouble(11), r.getDouble(12), r.getDouble(13), r.getDouble(14), r.getDouble(15)))
    rowsRDD.cache()

    // K 평균 모델을 트레이닝하기 위해 rdd로 변환하고 캐싱한다
    val landRDD = landDF.rdd.map(r => Vectors.dense(r.getDouble(1), r.getDouble(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getDouble(9), r.getDouble(10), r.getDouble(11), r.getDouble(12), r.getDouble(13), r.getDouble(14), r.getDouble(15)))
    landRDD.cache()

    // 이분법 K-평균을 사용해 데이터를 두 개의 클래스로 클러스터링한다.
    val bkm = new BisectingKMeans()
      .setK(5) // 비슷한 주택을 클러스터링한 개수
      .setMaxIterations(20)// 최대 반복 개수
      .setSeed(12345) // 랜덤을 허용하지 않기 위해 시드를 설정한다.
    val model = bkm.run(landRDD)

    // 비용(WCSS)을 계산해 클러스터링을 계산한다.
    val WCSS = model.computeCost(landRDD)
    println("Within-Cluster Sum of Squares = " + WCSS) // 작을수록 좋다.

    val end = System.currentTimeMillis()
    println("Model building and prediction time: "+ {end - start} + "ms")

    // 각 주택에 대해 예측 정확도를 계산하고 출력한다.
    model.predict(landRDD).foreach(println)
    landDF.show()

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

    spark.stop()
  }
}
