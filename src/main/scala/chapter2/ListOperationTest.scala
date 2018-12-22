package chapter2

object ListOperationTest {
   def main(args: Array[String]) {
      val animal1 = Set("cat", "dog", "rabbit")
      val animal2 = Set("lion", "elephant")
      // ++ 오퍼레이터로 두 셋을 합친다
      var animal = animal1 ++ animal2
      println( "animal1 ++ animal2 : " + animal )
      // .++ 오퍼레이터로 두 셋을 합친다
      animal = animal1.++(animal2)
      println( "animal1.++(animal2) : " + animal )
   }
}