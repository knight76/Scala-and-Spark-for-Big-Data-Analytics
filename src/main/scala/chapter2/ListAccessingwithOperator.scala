package chapter2

object ListAccessingwithOperator {
   def main(args: Array[String]) {
      val animal1: List[String] = List("cat", "dog", "rabbit")
      val animal2: List[String] = List("lion", "elephant")
      // ::: 오퍼레이터로 두 리스트를 합친다
      var animal = animal1 ::: animal2
      println( "animal1 ::: animal2 : " + animal )
      // Set.:::() 메소드로 두 리스트를 합친다
      animal = animal1.:::(animal2)
      println( "animal1.:::(animal2) : " + animal )
      // 두 리스트를 매개 변수로 전달해 합친다
      animal = List.concat(animal1, animal1)
      println( "List.concat(animal1, animal2) : " + animal  )
   }
}