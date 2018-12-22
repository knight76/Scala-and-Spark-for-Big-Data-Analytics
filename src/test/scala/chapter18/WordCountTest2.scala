package chapter18

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class WordCountTest2 extends FunSuite with BeforeAndAfterAll {
  var spark: SparkSession = null

  def tokenize(line: RDD[String]) = {
    line.map(x => x.split(' ')).collect()
  }

  override def beforeAll() {
    spark = SparkSession
      .builder
      .master("local[*]")
      .appName(s"wordCountTest2")
      .getOrCreate()
  }

  test("Test if two RDDs are equal") {
    val input = List("To be,", "or not to be:", "that is the question-", "William Shakespeare")
    val expected = Array(Array("To", "be,"), Array("or", "not", "to", "be:"), Array("that", "is", "the", "question-"), Array("William", "Shakespeare"))
    val transformed = tokenize(spark.sparkContext.parallelize(input))
    assert(transformed === expected)
  }

  test("Test for word count RDD") {
    val fileName = "data/words.txt"
    val obj = new WordCountRDD
    val result = obj.prepareWordCountRDD(fileName, spark)
    assert(result.count() === 210)
  }

  override def afterAll() {
    spark.stop()
  }
}
