package stralau.bilderbote

import com.typesafe.scalalogging.Logger
import org.scalatest.funsuite.AnyFunSuite
import stralau.bilderbote.Util.retry

import scala.concurrent.Future

class UtilTest extends AnyFunSuite{
  implicit val logger: Logger = Logger[UtilTest]

  test("Retries") {
    var counter = 1
    val action: () => Int = () => {
      if (counter > 0) {
        counter -= 1
        throw new RuntimeException("Exception message")
      }
      5
    }
    assert(retry(action)(1) == 5)
  }

  test("Fails after max number of retries") {

    var count = 0

    val action: () => Int = () =>  {
      count += 1
      throw new RuntimeException("Exception message")
    }

    assertThrows[RuntimeException](retry(action)(3))

    assert(count == 4)

  }
}
