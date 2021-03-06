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
      case Failure(ex) =>
        logger.info(s"$actionName failed: ${ex.getMessage}")
    }
  }

  def url(tweet: Tweet) = s"https://twitter.com/${tweet.user.get.screen_name}/status/${tweet.id}"

  def essentials(tweet: Tweet) = Seq(tweet.id, tweet.created_at, url(tweet)).mkString("Tweet(", ",", ")")

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

  def readEnv(key: String): String = {
    sys.env.get(key).map {
      value =>
        //        logger.info(s"Env[$key] = $value")
        value
    }.getOrElse(throw new RuntimeException(s"Could not find env var $key"))
  }

}
