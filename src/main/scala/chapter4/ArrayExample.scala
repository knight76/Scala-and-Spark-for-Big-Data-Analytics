package chapter4
import Array._

object ArrayExample {
  def main(args: Array[String]) {
    val numbers: Array[Int] = Array[Int](1, 2, 3, 4, 5, 1, 2, 3, 3, 4, 5) // 간단한 배열

    // 배열의 모든 엘리먼트를 출력한다
    println("The full array is: ")
    for (i <- numbers) {
      print(" " + i)
    }

    // 예를 들어 3번째 엘리먼트를 출력한다
    println(numbers(2))

    // 모든 엘리먼트의 합계를 구한다
    var total = 0
    for (i <- 0 to (numbers.length - 1)) {
      total = total + numbers(i)
    }
    println("Sum: = " + total)

    // 가장 작은 엘리먼트를 찾는다
    var min = numbers(0)
    for (i <- 1 to (numbers.length - 1)) {
      if (numbers(i) < min) min = numbers(i)
    }
    println("Min is: " + min)

    // 가장 큰 엘리먼트를 찾는다
    var max = numbers(0)
    for (i <- 1 to (numbers.length - 1)) {
      if (numbers(i) > max) max = numbers(i)
    }
    println("Max is: " + max)

    //range 메소드를 사용해 배열을 생성한다
    var myArray1 = range(5, 20, 2)
    var myArray2 = range(5, 20)

    // 모든 배열 엘리먼트를 출력한다
    for (x <- myArray1) {
      print(" " + x)
    }

    println()
    for (x <- myArray2) {
      print(" " + x)
    }

    //배열을 합친다
    var myArray3 = concat(myArray1, myArray2)
    // 모든 배열 엘리먼트를 출력한다
    for (x <- myArray3) {
      print(" " + x)
    }

    //2차원 배열을 출력한다
    var myMatrix = ofDim[Int](4, 4)
    // 2차원 배열을 생성한다
    for (i <- 0 to 3) {
      for (j <- 0 to 3) {
        myMatrix(i)(j) = j
      }
    }
    println()

    // 2차원 배열을 출력한다
    for (i <- 0 to 3) {
      for (j <- 0 to 3) {
        print(" " + myMatrix(i)(j))
      }
      println()
    }
  }
}
