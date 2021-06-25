package stralau.bilderbote

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Util {

  def log[T](action: () => Future[T], actionName: String)(implicit logger: Logger): Future[T] = {
    logger.info(s"Starting $actionName")
    action().andThen {
      case s: Success[T] =>
        logger.info(s"$actionName was successful")
        s
      case Failure(ex) =>
        logger.info(s"$actionName failed: ${ex.getMessage}")
        Failure(ex)
    }
  }

  def url(tweet: Tweet) = s"https://twitter.com/${tweet.user.get.screen_name}/status/${tweet.id}"

  def retry[T](action: () => Future[T])(times: Int)(implicit logger: Logger): Future[T] = {
    action().recoverWith { case exception: Exception if times > 0 =>
      logger.warn("Retrying after failure: " + exception.getMessage)
      retry(action)(times - 1)
    }.andThen {
      case Failure(exception) => logger.error(s"Failure: ${exception.getMessage}")
    }
  }

  def retry[T](action: () => T)(times: Int)(implicit logger: Logger): T = {
    Try(action()).recover {
      case ex: Exception if times > 0 =>
        logger.warn("Retrying after failure: " + ex.getMessage)
        retry(action)(times - 1)
      case ex: Exception =>
        logger.error(s"Failure: ${ex.getMessage}")
        throw ex
    }
  }.get

}
