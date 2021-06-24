package stralau.bilderbote

import com.typesafe.scalalogging.Logger
import org.scalatest.AsyncTestSuite
import org.scalatest.funsuite.{AnyFunSuite, AsyncFunSuite}
import stralau.bilderbote.Util.retry

import scala.concurrent.Future

class UtilTest extends AsyncFunSuite {

  implicit val logger: Logger = Logger[UtilTest]

  test("Retries") {
    var counter = 1
    val action = () => Future[Int] {
      if (counter > 0) {
        counter -= 1
        throw new RuntimeException("Exception message")
      }
      5
    }

    Util.retry(action)(1).map(result => assert(result == 5))
  }

  test("Fails after max number of retries") {

    var count = 0

    val action = () => Future[Int] {
      count += 1
      throw new RuntimeException("Exception message")
    }

    recoverToSucceededIf[RuntimeException](retry(action)(3)).map {
      _ => assert(count == 4)
    }

  }

}