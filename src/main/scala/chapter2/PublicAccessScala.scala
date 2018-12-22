package chapter2

class OuterClass {
   class InnerClass {
      def printName() { println("Hello world, my name is Asif Karim!") }
      
      class InnerMost {
         printName() // 동작
      }
   }
   (new InnerClass).printName() // printName이 public이기 때문에 문제 없다
}


package MyPackage {
   class SuperClass {
      private def printName() { println("Hello world, my name is Asif Karim!") }
   }
   
   class SubClass extends SuperClass {
      //printName() //에러
   }
   
   class SubsubClass {
      //(new SuperClass).printName() // 에러: printName에 접근할 수 없다
   }
}