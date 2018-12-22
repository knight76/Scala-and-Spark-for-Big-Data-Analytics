package chapter4

object ExistsExample {
  def main(args: Array[String]) {
    // 도시 리스트를 제공하고 도시 리스트에 Dublin이 포함되어 있는지 확인한다
    val cityList = List("Dublin", "NY", "Cairo")
    val ifExisitsinList = cityList exists (x => x == "Dublin")
    println(ifExisitsinList)

    // 맵에 나라와 도시를 저장하고 해당 맵에 Dublin이 포함되어 있는지 확인한다
    val cityMap = Map("Ireland" -> "Dublin", "UK" -> "London")
    val ifExistsinMap =  cityMap exists (x => x._2 == "Dublin")
    println(ifExistsinMap)
  }
}
