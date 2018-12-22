package chapter3

// 다음은 동료가 보낸 정의를 요약한 트레이트이다.
trait Curry {
  def curry[A, B, C](f: (A, B) => C): A => B => C
  def uncurry[A, B, C](f: A => B => C): (A, B) => C
}

object CurryImplement extends Curry {
  // 구현해야 할 함수 중 uncurry 함수가 이해하기 쉽기 때문에 먼저 구현한다
  // 등호 다음의 2개의 중괄호는 2개의 매개 변수를 받는 익명의 함수 리터럴이다.
  // 그리고 2개의 매개 변수는 함수를 리턴하는 함수에서도 사용될 수 있다.
  // 다음 2번째 매개 변수를 리턴될 함수에 전달하고 마지막으로 두 번째 함수의 값을 리턴한다.
  def uncurry[X, Y, Z](f: X => Y => Z): (X, Y) => Z = { (a: X, b: Y) => f(a)(b) }

  // 두 번째 함수 리터럴, 즉 curry 함수는 하나의 매개 변수를 받고 새로운 함수를 리턴한다.
  // 마지막으로 curry 함수는 호출될 때 다른 함수를 리턴하는 함수를 리턴한다.
  def curry[X, Y, Z](f: (X, Y) => Z): X => Y => Z = { (a: X) => { (b: Y) => f(a, b) } }
}

object RefactoringHigherOrderFunction {
  def main(args: Array[String]): Unit = {
    def add(x: Int, y: Long): Double = x.toDouble + y
    val addSpicy = CurryImplement.curry(add)
    println(addSpicy(3)(1L)) // "4.0"을 출력한다

    val increment = addSpicy(2) // increment은 long 타입의 2를 더하며 마지막으로 3.0을 출력하는 함수를 갖는다
    println(increment(1L)) // "3.0"을 출력한다

    val unspicedAdd = CurryImplement.uncurry(addSpicy)
    println(unspicedAdd(1, 6L)) // "7.0"을 출력한다
  }

}