package chapter4

object ListExample {
    def main(args: Array[String]) {
        // cities 리스트
        val cities = "Dublin" :: "London" :: "NY" :: Nil

        // List of Even Numbers
        val nums = 2 :: 4 :: 6 :: 8 :: Nil

        // empty 리스트
        val empty = Nil

        // 2차원 리스트
        val dim = 1 :: 2 :: 3 :: Nil ::
                  4 :: 5 :: 6 :: Nil ::
                  7 :: 8 :: 9 :: Nil :: Nil
        val temp = Nil

        // 리스트의 첫 번째 엘리먼트를 얻는다
        println( "Head of cities : " + cities.head )

        // 리스트의 마지막 엘리먼트를 얻는다
        println( "Tail of cities : " + cities.tail )

        //cities 및 temp 리스트가 비어 있는지 확인한다
        println( "Check if cities is empty : " + cities.isEmpty )
        println( "Check if temp is empty : " + temp.isEmpty )
     
        val citiesEurope = "Dublin" :: "London" :: "Berlin" :: Nil
        val citiesTurkey = "Istanbul" :: "Ankara" :: Nil

        //:::를 사용해 2개 이상의 리스트를 합친다
        var citiesConcatenated = citiesEurope ::: citiesTurkey
        println( "citiesEurope ::: citiesTurkey : "+citiesConcatenated )

        // concat 메소드를 사용한다
        citiesConcatenated = List.concat(citiesEurope, citiesTurkey)
        println( "List.concat(citiesEurope, citiesTurkey) : " + citiesConcatenated)
    }
}

