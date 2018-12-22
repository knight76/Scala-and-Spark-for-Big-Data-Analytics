package chapter2

class cPoint(val xc: Int, val yc: Int) {
   var x: Int = xc
   var y: Int = yc
   
   def oMove(dx: Int, dy: Int) {
      x = x + dx
      y = y + dy
      println ("Lcation X: " + x);
      println ("Location Y: " + y);
   }
}

object Dimension {
   def main(args: Array[String]) {
      // 실제 값(15, 27)을 넣어 cPoint 객체를 생성한다
      val pt = new cPoint(15, 27);

      // 이제 20,30 좌표의 새로운 장소로 이동한다
      pt.oMove(20, 30);
      
      // 이제 25,45 좌표의 새로운 장소로 이동한다
      pt.oMove(25, 45);
   }
}