package stralau.bilderbote

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.Logger

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


}
