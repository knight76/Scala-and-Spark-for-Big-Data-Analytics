package chapter3

object UsingRangeWithForLoop {
   def main(args: Array[String]):Unit= {
      var i = 0;      
      // 범위를 가진 루프 실행
      for( i <- 1 to 20){
         println( "Value of i: " + i )
      }
   }
}
