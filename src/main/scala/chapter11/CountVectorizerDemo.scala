package chapter11
import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel}
import org.apache.spark.sql.SparkSession

object CountVectorizerDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName(s"OneVsRestExample")
      .getOrCreate()

    val df = spark.createDataFrame(
      Seq((0, Array("Jason", "David")),
        (1, Array("David", "Martin")),
        (2, Array("Martin", "Jason")),
        (3, Array("Jason", "Daiel")),
        (4, Array("Daiel", "Martin")),
        (5, Array("Moahmed", "Jason")),
        (6, Array("David", "David")),
        (7, Array("Jason", "Martin")))).toDF("id", "name")

    df.show(false)

    // 코퍼스로부터 CountVectorizerModel를 피팅한다
    val cvModel: CountVectorizerModel = new CountVectorizer()
      .setInputCol("name")
      .setOutputCol("features")
      .setVocabSize(3)
      .setMinDF(2)
      .fit(df)

    val feature = cvModel.transform(df)
    feature.show(false)

    spark.stop()
  }
}