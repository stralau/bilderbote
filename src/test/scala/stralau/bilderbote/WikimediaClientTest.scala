package stralau.bilderbote

import org.scalatest.funsuite.AnyFunSuite

class WikimediaClientTest extends AnyFunSuite {

  val wikimediaClient = WikimediaClient()

  test("Removes suffix") {
    assert(wikimediaClient.removeSuffix("foo.jpg") == "foo")
  }

  test("Removes no string inside") {
    assert(wikimediaClient.removeSuffix("foo.jpg.bar") == "foo.jpg.bar")
  }

  test("Removes only one suffix") {
    assert(wikimediaClient.removeSuffix("foo.jpg.png") == "foo.jpg")
  }

}
