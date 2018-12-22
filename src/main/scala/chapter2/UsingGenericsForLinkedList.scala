package chapter2

class UsingGenericsForLinkedList[X] { // 다른 값을 출력할 수 있도록 사용자 별 링크드 리스트를 생성한다
  private class Node[X](elem: X) {
    var next: Node[X] = _
    override def toString = elem.toString
  }

  private var head: Node[X] = _

  def add(elem: X) { //링크드 리스트에 엘리먼트를 추가한다
    val value = new Node(elem)
    value.next = head
    head = value
  }

  private def printNodes(value: Node[X]) { // 노드의 값을 출력한다
    if (value != null) {
      println(value)
      printNodes(value.next)
    }
  }

  def printAll() { printNodes(head) } //한 번에 모든 노드의 값을 출력한다

}

object UsingGenericsForLinkedList {
  def main(args: Array[String]) {
    // 정수형 리스트를 갖는  UsingGenericsForLinkedList 클래스를 생성하기 위해  
    // 먼저 타입을 Int로 갖는 UsingGenericsForLinkedList 인스턴스를 생성한다
    val ints = new UsingGenericsForLinkedList[Int]()

    // 그 다음 정수 값을 추가한다
    ints.add(1)
    ints.add(2)
    ints.add(3)
    ints.printAll()

    // 클래스는 제네릭 타입을 사용하기 때문에 String 타입의 링크드 리스트를 생성할 수도 있다
    val strings = new UsingGenericsForLinkedList[String]()
    strings.add("Salman Khan")
    strings.add("Aamir Khan")
    strings.add("Shah Rukh Khan")
    strings.printAll()

    // 또는 Double 타입과 같은 다른 타입의 링크드 리스트를 생성할 수도 있다
    val doubles = new UsingGenericsForLinkedList[Double]()
    doubles.add(10.50)
    doubles.add(25.75)
    doubles.add(12.90)
    doubles.printAll()
  }
}
