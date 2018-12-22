package chapter3
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
object RunOneTaskbutBlock {
  def main(args: Array[String]) {
    // 현재 시간을 밀리 세컨드로 얻는다
    implicit val baseTime = System.currentTimeMillis    
    // 함수 생성
    val testFuture = Future {
      Thread.sleep(300)
      2 + 2
    }
    // Future를 생성한다
    val finalOutput = Await.result(testFuture, 2 second)
    println(finalOutput)
  }
}