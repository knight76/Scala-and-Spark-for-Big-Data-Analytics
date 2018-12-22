package chapter2

object CaseClass {
  def main(args: Array[String]) {
    case class Character(name: String, isHacker: Boolean) // 특정인이 컴퓨터 해커라면 클래스를 정의한다

    // Nail은 해커이다
    val nail = Character("Nail", true)

    // 이제 변경 요청 사항이 있는 인스턴스의 복사본을 리턴한다
    val joyce = nail.copy(name = "Joyce")

    // Nail과 Joyce가 동일한지 확인한다
    println(nail == joyce)

    // Nail과 Nail이 동일한지 확인한다
    println(nail.equals(joyce))

    // Nail과 Nail이 동일한지 확인한다
    println(nail.equals(nail))

    // Nail의 해싱 코드를 확인한다
    println(nail.hashCode())

    // Nail의 해싱 코드를 확인한다
    println(nail)

    joyce match {
      case Character(x, true) => s"$x is a hacker"
      case Character(x, false) => s"$x is not a hacker"
    }
  }
}