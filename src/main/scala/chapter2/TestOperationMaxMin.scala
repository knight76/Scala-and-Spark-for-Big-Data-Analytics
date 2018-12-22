package chapter2

object ListOperationMaxMinTest {
   def main(args: Array[String]) {
      val numbers = Set(1,2,5,0,10,4)
      // 엘리먼트의 최솟값과 최댓값을 찾는다
      println( "The Minimum of the set Set(1,2,5,0,10,4) : " + numbers.min )
      println( "The Maximum of the set Set(1,2,5,0,10,4): " + numbers.max )
   }
}