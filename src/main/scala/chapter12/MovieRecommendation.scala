package chapter12

import org.apache.spark.sql.SparkSession
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
import scala.Tuple2
import org.apache.spark.rdd.RDD

object MovieRecommendation {
  // 모델을 계산하기 위해 RMSE를 계산한다. RMSE가 적으면 모델이 좋고 예측 기능이 좋다
  def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating], implicitPrefs: Boolean): Double = {
    val predictions: RDD[Rating] = model.predict(data.map(x => (x.user, x.product)))
    val predictionsAndRatings = predictions.map { x => ((x.user, x.product), x.rating)
    }.join(data.map(x => ((x.user, x.product), x.rating))).values
    if (implicitPrefs) {
      println("(Prediction, Rating)")
      println(predictionsAndRatings.take(5).mkString("\n"))
    }
    math.sqrt(predictionsAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).mean())
  }

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("JavaLDAExample")
      .master("local[*]")
      .getOrCreate()

    val ratigsFile = "data/ratings.csv"
    val df1 = spark.read.format("com.databricks.spark.csv").option("header", true).load(ratigsFile)

    val ratingsDF = df1.select(df1.col("userId"), df1.col("movieId"), df1.col("rating"), df1.col("timestamp"))
    ratingsDF.show(false)

    val moviesFile = "data/movies.csv"
    val df2 = spark.read.format("com.databricks.spark.csv").option("header", "true").load(moviesFile)

    val moviesDF = df2.select(df2.col("movieId"), df2.col("title"), df2.col("genres"))
    moviesDF.show(false)

    ratingsDF.createOrReplaceTempView("ratings")
    moviesDF.createOrReplaceTempView("movies")

    // 스파크 데이터 프레임으로 쿼리한다
    val numRatings = ratingsDF.count()
    val numUsers = ratingsDF.select(ratingsDF.col("userId")).distinct().count()
    val numMovies = ratingsDF.select(ratingsDF.col("movieId")).distinct().count()
    println("Got " + numRatings + " ratings from " + numUsers + " users on " + numMovies + " movies.")

    // 영화 평가 점수를 매긴 사용자 수와 함께 최대, 최소 평가점수를 얻는다
    val results = spark.sql("select movies.title, movierates.maxr, movierates.minr, movierates.cntu "
      + "from(SELECT ratings.movieId,max(ratings.rating) as maxr,"
      + "min(ratings.rating) as minr,count(distinct userId) as cntu "
      + "FROM ratings group by ratings.movieId) movierates "
      + "join movies on movierates.movieId=movies.movieId " + "order by movierates.cntu desc");

    results.show(false)

    // 가장 활동적인 상위 10위 사용자와 영화 평가 횟수
    val mostActiveUsersSchemaRDD = spark.sql("SELECT ratings.userId, count(*) as ct from ratings "
                                              + "group by ratings.userId order by ct desc limit 10")
    mostActiveUsersSchemaRDD.show(false)

    // 668 사용자가 평점 매긴 영화 중 평점 4보다 큰 영화를 검색한다
    val results2 = spark.sql(
                "SELECT ratings.userId, ratings.movieId," 
                + "ratings.rating, movies.title FROM ratings JOIN movies "
                + "ON movies.movieId=ratings.movieId " 
                + "where ratings.userId=668 and ratings.rating > 4")
        
    results2.show(false)

    // 평점 RDD를 랜덤하게 데이터 RDD(75%), 테스트 데이터(25%)로 분할한다
    val splits = ratingsDF.randomSplit(Array(0.75, 0.25), seed = 12345L)
    val (trainingData, testData) = (splits(0), splits(1))
    val numTraining = trainingData.count()
    val numTest = testData.count()
    println("Training: " + numTraining + " test: " + numTest)

    val ratingsRDD = trainingData.rdd.map(row => {
      val userId = row.getString(0)
      val movieId = row.getString(1)
      val ratings = row.getString(2)
      Rating(userId.toInt, movieId.toInt, ratings.toDouble)
    })

    val testRDD = testData.rdd.map(row => {
      val userId = row.getString(0)
      val movieId = row.getString(1)
      val ratings = row.getString(2)
      Rating(userId.toInt, movieId.toInt, ratings.toDouble)
    })

    /*
     * 영화 평점 데이터를 이용해 ALS를 적용한다. rank=20, iterations=15인 ALS 사용자 메트릭스 모델을 구축한다.
     * 트레이닝은 협업 필터링의 ALS 알고리즘을 통해 수행한다.
     * 기본적으로 매트릭스 모델 기술은 다른 영화에 비슷한 평점을 준 다른 사용자가 평가한 평점을 기반으로 특정 영화의 특정 사용자에 대해 누락된 평점을 예측한다.
     * 자세한 내용은 문서를 ALS 문서를 참고하길 바란다.
     */
    val rank = 20
    val numIterations = 15
    val lambda = 0.10
    val alpha = 1.00
    val block = -1
    val seed = 12345L
    val implicitPrefs = false 
    
    val model = new ALS()
                  .setIterations(numIterations)
                  .setBlocks(block)
                  .setAlpha(alpha)
                  .setLambda(lambda)
                  .setRank(rank)
                  .setSeed(seed)
                  .setImplicitPrefs(implicitPrefs)
                  .run(ratingsRDD)
                 
    //val model = ALS.train(ratingsRDD, rank, numIterations, lambda, alpha, false)

    //Saving the model for future use
    //val num = model.save(spark.sparkContext, "output/MovieRecomModel")

    // 예측한다. 사용 668에 대한 상위 6개의 영화 예측을 얻는다
    println("Rating:(UserID, MovieID, Rating)")
    println("----------------------------------")
    val topRecsForUser = model.recommendProducts(668, 6)
    for (rating <- topRecsForUser) {
      println(rating.toString())
    }
    println("----------------------------------")

    /**
      * 모델을 평가한다.
      * 모델의 품질을 검증하기 위해 평균 제곱근 편차(RMSE, Root Mean Squared Error)를 사용해 모델로 예측한 값과 실제 관찰한 값 간의 차이를 측정한다.
      * 기본적으로 계산된 오차가 작을수록 모델이 더 좋다. 모델의 품질을 테스트하기 위해 테스트 데이터가 사용된다(이전 단계에서 분할된다).
      * RMSE는 정확도에 대한 좋은 척도지만 규모에 따라 달라질 수 있어서 변수 사이가 아닌 특정 변수에 대한 다른 모델의 예측 오차를 비교하는 것이다.
      */
    var rmseTest = computeRmse(model, testRDD, true)
    println("Test RMSE: = " + rmseTest) //Less is better

    // 특정 사용자의 영화 추천 정보. 668 사용자의 상위 6개의 영화를 예측한다
    println("Recommendations: (MovieId => Rating)")
    println("----------------------------------")
    val recommendationsUser = model.recommendProducts(668, 6)
    recommendationsUser.map(rating => (rating.product, rating.rating)).foreach(println)
    println("----------------------------------")

    spark.stop()
  }
}
