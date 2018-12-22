package chapter2

class Animal {
  var animalName = "notset"
  var animalAge = -1
  def setAnimalName(animalName: String) {
    this.animalName = animalName
  }
  
  def setAnaimalAge(animalAge: Int) {
    this.animalAge = animalAge
  }
  
  def getAnimalName(): String = {
    animalName
  }
  
  def getAnimalAge(): Int = {
    animalAge
  }
}

object RunAnimalExample extends App {
  val animalObj = new Animal
  println(animalObj.getAnimalName)
  println(animalObj.getAnimalAge)
  
  // 이제 다음처럼 동물 이름과 나이를 변경한다
  animalObj.setAnimalName("dog")
  animalObj.setAnaimalAge(10)
  println(animalObj.getAnimalName)
  println(animalObj.getAnimalAge)
}
