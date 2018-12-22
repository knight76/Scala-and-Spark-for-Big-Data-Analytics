package chapter3
object PureandNonPureFunction {
  def pureFunc(cityName: String) = s"I live in $cityName"
  def notpureFunc(cityName: String) = println(s"I live in $cityName")
  def pureMul(x: Int, y: Int) = x * y
  def notpureMul(x: Int, y: Int) = println(x * y)    
  def main(args: Array[String]) {
    // 이제 모든 메소드에 실제 값을 전달한다
    pureFunc("Galway") // 아무 것도 출력하지 않는다
    notpureFunc("Dublin") // I live in Dublin을 출력한다
    pureMul(10, 25) // 아무 것도 출력하지 않는다
    notpureMul(10, 25) // 곱 결과(250)를 출력한다
    //이제 pureMul 메소드에 다른 방식으로 호출한다
    val data = Seq.range(1,10).reduce(pureMul)
    println(s"My sequence is: " + data)
  }
}