package chapter2

object MapTestDemo {
   def main(args: Array[String]) {
      val colorswithHex1 = Map("red" -> "#FF0000", "azure" -> "#F0FFFF", "peru" -> "#CD853F")
      val colorswithHex2 = Map("blue" -> "#0033FF", "yellow" -> "#FFFF00", "red" -> "#FF0000")
      // ++ 오퍼레이터로 두 맵을 합친다
      var colorswithHex = colorswithHex1 ++ colorswithHex2
      println( "colorswithHex1 ++ colorswithHex2 : " + colorswithHex )
      // ++ 오퍼레이터로 두 맵을 합친다
      colorswithHex = colorswithHex1.++(colorswithHex2)
      println( "colorswithHex1.++(colorswithHex2)) : " + colorswithHex )
   }
}