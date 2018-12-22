package chapter2

object ListScalaDemo {
  def main(args: Array[String]) {
    // 문자열 리스트
    val animal: List[String] = List("cat", "dog", "rabbit")
    
    // Int 리스트
    val number_list: List[Int] = List(2, 4, 6, 8)
    
    // 빈 리스트
    val empty: List[Nothing] = List()
    
    // 2 차원 리스트
    val numbers2D: List[List[Int]] =
      List(
        List(2, 4, 6),
        List(8, 10, 12),
        List(14, 16, 18))
        
    val numbers = Nil
    println("Complete integers : " + number_list)
    println("Head of integers : " + number_list.head)
    println("Tail of integers : " + number_list.tail)    
            
    println("Complete animals : " + animal)
    println("Head of animal : " + animal.head)
    println("Tail of animal : " + animal.tail)
    
    println("Check if animal is empty : " + animal.isEmpty)
    println("Check if numbers is empty : " + numbers.isEmpty)
  }
}