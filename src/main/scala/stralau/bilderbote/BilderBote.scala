package stralau.bilderbote

import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.MediaTypes.{`image/gif`, `image/jpeg`, `image/png`}
import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.TwitterImageClient.knownMediaTypes
import stralau.bilderbote.Util.retry

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.implicitConversions

object BilderBote {

  val twitterMaxImageSize = 5242880

  private implicit val logger: Logger = Logger("application")

  private val wikimediaClient = WikimediaClient()
  private val twitterImageClient = TwitterImageClient()
  private val twitterAttributionClient = TwitterAttributionClient()

  def main(args: Array[String]): Unit = run

  def run: Tweet = {
    val image = fetchImage
    val createTweets =
      retry(() => twitterImageClient.post(image))(3)
        .flatMap(tweet =>
          retry(() => twitterAttributionClient.tweetAttribution(image, tweet))(3)
            .recoverWith {
              case _ =>
                logger.info("Cleaning up after Failure")
                twitterImageClient.deleteTweet(tweet)
            }
        )
    Await.result(createTweets, 2.minutes)
  }

  private def fetchImage: WikimediaObject =
    wikimediaClient
      .getMetadata(wikimediaClient.fetchRandomFileLocation)
      .flatMap(validate)
    match {
      case Left(error) =>
        logger.warn(error)
        fetchImage
      case Right(image) => image
    }

  private def validate(image: WikimediaObject): Either[String, WikimediaObject] =
    validateMediaType(image).flatMap(validateSize)

  private def validateSize(image: WikimediaObject): Either[String, WikimediaObject] =
    if (image.image.length <= twitterMaxImageSize) Right(image)
    else Left("Image size too large")

  private def validateMediaType(image: WikimediaObject): Either[String, WikimediaObject] = {
    if (knownMediaTypes.contains(image.mediaType)) Right(image)
    else Left("Wrong media type")
  }

}