package chapter2

class ScalaGenericsForStackt[X] {
  private var elements: List[X] = Nil
  def push(x: X) { elements = x :: elements }
  def peek: X = elements.head
  def pop(): X = {
    val currentTop = peek
    elements = elements.tail
    currentTop
  }
}

object ScalaGenericsForStackt {
  def main(args: Array[String]):Unit = {
    val stack = new ScalaGenericsForStackt[Int]
    stack.push(1)
    stack.push(2)
    stack.push(3)
    stack.push(4)

    println(stack.pop) // 4를 출력한다
    println(stack.pop) // 3를 출력한다
    println(stack.pop) // 2를 출력한다
    println(stack.pop) // 1를 출력한다
  }
}
