package chapter18

import org.apache.spark.sql.SparkSession

object DebugTestSBT {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName("Logging")
      .getOrCreate()
      
    spark.sparkContext.setCheckpointDir("/")

    println("-------------Attach debugger now!--------------")
    Thread.sleep(8000)

    // 코드를 아래에 두고 멈추고 싶은 라인에 브레이크 포인트를 건다
  }
}
