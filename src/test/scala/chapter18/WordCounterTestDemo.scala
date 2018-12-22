package chapter18

import org.apache.spark.sql.SparkSession

class WordCounterTestDemo {
  val spark = SparkSession
    .builder
    .master("local[*]")
    .appName(s"OneVsRestExample")
    .getOrCreate()

  def myWordCounter(fileName: String): Long = {
    val input = spark.sparkContext.textFile(fileName)
    val counts = input.flatMap(_.split(" ")).distinct()
    val counter = counts.count()
    counter
  }
}