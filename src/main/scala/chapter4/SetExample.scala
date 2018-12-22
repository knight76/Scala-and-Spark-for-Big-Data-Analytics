package chapter4

object SetExample {
    def main(args: Array[String]) {
      // 정수 타입의 빈 셋
      var sInteger : Set[Int] = Set()

      // 짝수 숫자의 셋
      var sEven : Set[Int] = Set(2,4,8,10)

      // 아니면 다음 구문을 사용할 수 있다
      var sEven2 = Set(2,4,8,10)

      val cities = Set("Dublin", "London", "NY")
      val tempNums: Set[Int] = Set()

      // head, tail를 찾고 셋이 비어 있다면 확인한다
      println( "Head of cities : " + cities.head )
      println( "Tail of cities : " + cities.tail )
      println( "Check if cities is empty : " + cities.isEmpty )
      println( "Check if tempNums is empty : " + tempNums.isEmpty )

      val citiesEurope = Set("Dublin", "London", "NY")
      val citiesTurkey = Set("Istanbul", "Ankara")

      // ++ 연산자를 사용해 셋을 하나로 합친다
      var citiesConcatenated = citiesEurope ++ citiesTurkey
      println( "citiesEurope ++ citiesTurkey : " + citiesConcatenated )

      // 또한 메소드로 ++를 사용할 수 있다.
      citiesConcatenated = citiesEurope.++(citiesTurkey)
      println( "citiesEurope.++(citiesTurkey) : " + citiesConcatenated )

      //셋의 최대 및 최소 엘리먼트를 찾는다
      val evenNumbers = Set(2,4,6,8)

      // min과 max 메소드를 사용한다
      println( "Minimum element in Set(2,4,6,8) : " + evenNumbers.min )
      println( "Maximum element in Set(2,4,6,8) : " + evenNumbers.max )
    }
}
