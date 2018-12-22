package chapter4

object TupleExample {
   def main(args: Array[String]) {
      val evenTuple = (2,4,6,8)
      val sumTupleElements =evenTuple._1 + evenTuple._2 + evenTuple._3 + evenTuple._4

      println( "Sum of Tuple Elements: "  + sumTupleElements )

      // foreach 메소드를 사용해 튜플을 순회하고 튜플의 엘리먼트를 출력할 수 있다.
      evenTuple.productIterator.foreach{ evenTuple =>println("Value = " + evenTuple )}

   }
}

