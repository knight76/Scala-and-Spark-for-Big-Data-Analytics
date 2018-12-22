package chapter3
object flatMapExample {
    def main(args: Array[String]) {
      val eventList = List(2, 4, 6, 8, 10) // 간단한 리스트
      println("Original list: "+ eventList)      
      // map을 사용한다
      def around(x: Int) = List(x-1, x, x+1)
      val newList1 = eventList.map(x=> around(x))
      println("New list from map : " + newList1)      
      // flatMap을 사용한다
      val newList2 = eventList.flatMap(x=> around(x))
      println("New list from flatMap: " + newList2)
    }  
}