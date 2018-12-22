package chapter2

object Immutability {
  def main (args: Array[String]) {
    var testVar = 10

    // 다음 코드는 잘 동작한다
    testVar = testVar + 10
    var testVal = 6

    // testVal을 val로 선언한다면 컴파일 중에 에러가 발생할 것이다
    testVal = testVal + 10
    
    println(testVal)
  }
}