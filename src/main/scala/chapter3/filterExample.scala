package chapter3
object filterExample {
    def main(args: Array[String]) {
      val range = List.range(1, 10)
      println(range)      

      // 홀수만 필터링한다
      val odds = range.filter( x=> x % 2 != 0)
      println("Odd values: " + odds)

      // 짝수만 필터링한다
      val even = range.filter( x=> x % 2 == 0)
      println("Odd values: " + even)
    }
}