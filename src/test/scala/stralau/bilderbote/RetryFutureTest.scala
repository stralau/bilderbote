package stralau.bilderbote

import com.typesafe.scalalogging.Logger
import org.scalatest.funsuite.AsyncFunSuite
import stralau.bilderbote.Util.retry

import scala.concurrent.Future

class RetryFutureTest extends AsyncFunSuite {

  implicit val logger: Logger = Logger[RetryFutureTest]

  test("Retries Future") {
    var counter = 1
    val action = () => Future[Int] {
      if (counter > 0) {
        counter -= 1
        throw new RuntimeException("Exception message")
      }
      5
    }

    retry(action)(1).map(result => assert(result == 5))
  }

  test("Fails Future after max number of retries") {

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
