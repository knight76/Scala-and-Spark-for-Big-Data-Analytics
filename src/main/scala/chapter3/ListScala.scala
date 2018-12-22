package chapter3

object ListScala {
    def main(args: Array[String]) {
      val eventList = List(2, 4, 6, 8, 10) // 간단한 리스트
      val mappedList = eventList.map(x => x*2) // map을 사용해 각 값에 2를 곱한다
      println("Original list: "+ eventList)
      println("Mapped list: "+ mappedList)
      
      // 함수를 받아 리스트를 리턴하는 map을 사용한다
      def func(x: Int) = if (x > 4) Some(x) else None
      val newList = eventList.map(x=> func(x))
      println("New list: " + newList)
    }  
}