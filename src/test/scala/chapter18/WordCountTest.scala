package chapter18

import org.apache.spark.sql.SparkSession
import org.junit.Test
import org.scalatest.Assertions._

class WordCountTest {
  val spark = SparkSession
    .builder
    .master("local[*]")
    .appName(s"wordCountTest")
    .getOrCreate()

    @Test def test() {
      val fileName = "../../data/words.txt"
      val obj = new WordCounterTestDemo()
      assert(obj.myWordCounter(fileName) == 210)
    }

    spark.stop()
}