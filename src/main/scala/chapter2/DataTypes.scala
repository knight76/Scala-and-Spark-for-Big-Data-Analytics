package chapter2

object DataTypes {
  def main(args: Array[String]) {
    var myVal = 20

    // println 메소드를 사용해 콘솔에 출력한다. myVal의 타입이 Int로 추론되는지 알 수 있을 것이다
    println(myVal + 10)

    myVal = 40
    // 만약 다음 라인을 실행하면 컴파일 오류가 발생할 것이다
    // println(myVal * "test")
  }
}