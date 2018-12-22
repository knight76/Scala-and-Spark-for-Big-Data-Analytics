package chapter3
object HigherOrderFunction {
  def quarterMaker(value: Int): Double = value.toDouble / 4
  def testHOF(func: Int => String, value: Int) = func(value)
  def paramFunc[A](x: A) = "[" + x.toString() + "]"
  def addTwo(value: Int): Int = value + 2
  def applyFuncOnRange(begin: Int, end: Int, func: Int => AnyVal): Unit = {
    for (i <- begin to end)
      println(func(i))
  }
  def transferMoney(money: Double, bankFee: Double => Double): Double = {
    money + bankFee(money)
  }
  def bankFee(amount: Double) = amount * 0.05
  def main(args: Array[String]) {
    //이제 모든 메소드에 실제 값을 적용한다
    println(testHOF(paramFunc, 10)) // [10]을 출력한다
    println(quarterMaker(20)) // 5.0을 출력한다
    println(paramFunc(100)) // [100]을 출력한다
    println(addTwo(90)) // 92을 출력한다
    println(applyFuncOnRange(1, 20, addTwo)) // 3부터 22까지, ()을 출력한다
    println(transferMoney(105.0, bankFee)) // 110.25을 출력한다
  }
}
