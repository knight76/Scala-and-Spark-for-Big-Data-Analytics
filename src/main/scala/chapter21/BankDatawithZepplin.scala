package com.example.ZeepleinAndSpark

import org.apache.spark.sql.{SQLContext, SparkSession}

object BankDatawithZepplin {

  case class Bank(age: Integer, job: String, marital: String, education: String, balance: Integer)

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("BankDatawithZepplin")
      .master("local[*]")
      .getOrCreate();

    val bankText = spark.sparkContext.textFile("data/bank-full.csv")

    // 헤더(age로 시작)를 제외한 각 라인을 분리하고 Back 케이스 클래스로 매핑한다
    val bank = bankText.map(s => s.split(";")).filter(s => (s.size) > 5).filter(s => s(0) != "\"age\"").map(
      s => Bank(s(0).toInt,
        s(1).replaceAll("\"", ""),
        s(2).replaceAll("\"", ""),
        s(3).replaceAll("\"", ""),
        s(5).replaceAll("\"", "").toInt))

    val sqlContext = new SQLContext(spark.sparkContext)

    import sqlContext.implicits._

    // 데이터 프레임으로 변환하고 임시 테이블을 생성한다
    val newDF = bank.toDF()
    newDF.show()
    newDF.createOrReplaceTempView("bank")

    spark.sql("select age, count(1) from bank where age <= 50 group by age order by age").show()

    spark.sql("select age, count(1) from bank where age <= 30 group by age order by age").show()
    spark.sql("select max(balance) as MaxBalance from bank where age <= 30 group by age order by MaxBalance DESC").show()

  }
}