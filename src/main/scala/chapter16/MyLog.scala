package chapter16

import org.apache.log4j.{Level, LogManager}
import org.apache.spark.{SparkConf, SparkContext}

object MyLog1 extends Serializable {
 def main(args: Array[String]):Unit= {
   // 로거 레벨을 WARN으로 설정한다
   val log = LogManager.getRootLogger
   log.setLevel(Level.WARN)
   @transient lazy val log2 = org.apache.log4j.LogManager.getLogger("myLogger")

   // SparkContext을 생성한다
   val conf = new SparkConf().setAppName("My App").setMaster("local[*]")
   val sc = new SparkContext(conf)

   //계산한 후 로깅 정보를 출력한다
   //log.warn("Started")
   //val i = 0
   val data = sc.parallelize(0 to 100000)
   data.foreach(i => log.info("My number"+ i))
   data.collect()
   log.warn("Finished")
 }
}