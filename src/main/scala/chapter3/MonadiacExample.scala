package chapter3
object MonadiacExample {
  def main(args: Array[String]) {
    // Monadiac 예 1
    for (x <- 10 until (0, -2))
       yield x
    //Monadiac 예 2
    for (x <- 1 to 10 if x % 2 == 0)
      yield x
    // Monodiac 예 3
    for (x <- 1 to 10; y <- 1 until x)
      yield (x, y)
    (1 to 10).flatMap(i => (1 until i).map(j => (i, j)))
  }
}