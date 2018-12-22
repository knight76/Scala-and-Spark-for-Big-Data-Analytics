package chapter18

import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.SparkSession

object MyCustomLogwithClosure extends Serializable {
 def main(args: Array[String]): Unit = {   
    val log = LogManager.getRootLogger

    // 로그 레벨을 INFO로 설정한다. WARN으로 설정할 때까지 모든 INFO 로그가 출력된다.
    log.setLevel(Level.INFO)
    log.info("Let's get started!")

    // 로그 레벨을 WARN으로 설정한다
    log.setLevel(Level.WARN)

    // 스파크 세션을 생성한다
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName("Logging")
      .getOrCreate()

    // 출력되지 않는다
    log.info("Get prepared!")
    log.trace("Show if there is any ERROR!")

    // 계산이 되고 로깅 정보가 출력된다
    log.warn("Started")
    val data = spark.sparkContext.parallelize(0 to 100000)
    data.foreach(i => log.info("My number"+ i))
    data.collect()
    log.warn("Finished")
 }
}


 