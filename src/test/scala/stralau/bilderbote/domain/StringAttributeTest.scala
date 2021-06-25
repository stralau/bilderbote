package stralau.bilderbote.domain

import org.scalatest.funsuite.AnyFunSuite

class StringAttributeTest extends AnyFunSuite {

  case class Attribute(source: String) extends StringAttribute

  test("Takes from the beginning") {
    val a = Attribute("Hello, world")
    assert(a.take(6) == "Hello,")
  }

  test(("Cleans from URLEncoding")) {
    val a = Attribute("Hello%2C%20world")
    assert(a.toString == "Hello, world")
  }

  test("Cleans from HTML") {
    val a = Attribute("<span id=\"1234\">Hello, </span>world")
    assert(a.toString == "Hello, world")
  }

  test("Strips lower case suffix") {
    val n = Name("Hello, world.jpg")
    assert(n.toString == "Hello, world")
  }

  test("Strips upper case suffix") {
    val n = Name("Hello, world.JPG")
    assert(n.toString == "Hello, world")
  }

  test("Strips only one suffix") {
    val n = Name("Hello, world.jpg.png")
    assert(n.toString == "Hello, world.jpg")
  }

  test("Strips no string inside") {
    val n = Name("Hello, world.jpg.bar")
    assert(n.toString == "Hello, world.jpg.bar")
  }
}
