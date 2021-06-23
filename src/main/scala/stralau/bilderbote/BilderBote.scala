package stralau.bilderbote

import akka.http.scaladsl.model.{MediaType, MediaTypes}
import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

object BilderBote {

  val twitterMaxImageSize = 5242880

  private val logger = Logger("application")

  private val wikimediaClient = WikimediaClient()
  private val twitterImageClient = TwitterImageClient()
  private val twitterAttributionClient = TwitterAttributionClient()

  def main(args: Array[String]): Unit = run

  def run: Tweet = {
    val image = fetchImage
    val f =
      retry(() => twitterImageClient.post(image))(3)
        .flatMap (tweet =>
          retry(() => twitterAttributionClient.tweetAttribution(image,tweet))(3)
            .recoverWith {
              case _ =>
                logger.info("Cleaning up after Failure")
                twitterImageClient.deleteTweet(tweet)
            }
        )
    Await.result(f, 2.minutes)
  }

  private def fetchImage: WikimediaObject = {
    wikimediaClient.getMetadata(wikimediaClient.fetchRandomFileLocation) match {
      case Right(image) if valid(image) => image
      case Right(_) => fetchImage
      case Left(error) =>
        logger.info(s"Error when fetching image: $error")
        fetchImage
    }
  }


  private def valid(image: WikimediaObject): Boolean = {
    if (!knownMediaType(image)) {
      logger.warn("Wrong mediaType")
      return false
    }
    if (!imageSize(image)) {
      logger.warn("Image size too large")
      return false
    }
    true
  }

  private def imageSize(image: WikimediaObject) =
    image.image.length <= twitterMaxImageSize

  private def knownMediaType(image: WikimediaObject) = List(
    MediaTypes.`image/jpeg`,
    MediaTypes.`image/png`,
    MediaTypes.`image/gif`
  ).contains(image.mediaType)


  private def retry[T](action: () => Future[T])(times: Int): Future[T] = {
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