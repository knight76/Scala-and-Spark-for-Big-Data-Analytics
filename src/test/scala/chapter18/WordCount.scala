package chapter18

import org.apache.spark.sql.SparkSession

object WordCount {
  val spark = SparkSession
    .builder
    .master("local[*]")
    .appName("WordCount")
    .getOrCreate()
    
  val fileName = "data/words.txt"

  def myWordCounter(fileName: String): Long = {
    val input = spark.sparkContext.textFile(fileName)
    val counts = input.flatMap(_.split(" ")).distinct()
    val counter = counts.count()
    counter
  }

  def main(args: Array[String]): Unit = {
    val counter = myWordCounter(fileName)
    println("Number of words: " + counter)
  }
}