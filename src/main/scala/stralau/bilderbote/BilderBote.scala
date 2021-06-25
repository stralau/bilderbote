package stralau.bilderbote

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.Util.retry
import stralau.bilderbote.domain.WikimediaObject

import scala.annotation.tailrec
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
    val createTweets = retry { () =>
      val image = retry(() => fetchImage)(3)
      retry(() => twitterImageClient.post(image))(3)
        .flatMap(tweet =>
          retry(() => twitterAttributionClient.tweetAttribution(image, tweet))(3)
            .recoverWith {
              case _ =>
                logger.info("Cleaning up after Failure")
                twitterImageClient.deleteTweet(tweet)
            }
        )
    }(3)
    Await.result(createTweets, 2.minutes)
  }

  @tailrec
  private def fetchImage: WikimediaObject =
    wikimediaClient
      .getMetadata(wikimediaClient.fetchRandomFileLocation)
      .flatMap(_.validate)
    match {
      case Left(error) =>
        logger.warn(error)
        fetchImage
      case Right(image) => image
    }

}