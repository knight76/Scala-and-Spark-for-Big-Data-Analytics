package chapter18

import org.scalatest.Assertions._

object ExpectedException {
  def main(args: Array[String]):Unit= {
    val s = "Hello world!"
    try {
      s.charAt(0)
      fail()
    } catch {
      case _: IndexOutOfBoundsException => // 예상된 예외이고 계속 진행된다
    }
  }
}