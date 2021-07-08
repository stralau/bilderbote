package stralau.bilderbote

import org.scalatest.funsuite.AnyFunSuite

class XMLTest extends AnyFunSuite {

  test(".text unescapes") {
    val xml = <foo><bar>Me &amp; you</bar></foo>
    val text = (xml \ "bar").text
    assert(text == "Me & you")
  }

}
