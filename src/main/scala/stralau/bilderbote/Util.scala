package stralau.bilderbote

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.BilderBote.logger

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

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
    action().andThen {
      case Success(t) => t
      case Failure(exception) if times > 0 =>
        logger.warn("Retrying after failure: " + exception.getMessage)
        retry(action)(times - 1)
      case Failure(exception) =>
        logger.error("Failure: " + exception.getMessage)
    }
  }

}
