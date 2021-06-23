package stralau.bilderbote

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.Util.{log, url}

import java.io.ByteArrayInputStream
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

object TwitterImageClient {

  def apply() = new TwitterImageClient(TwitterRestClient())

}

class TwitterImageClient(tweetClient: TwitterRestClient) {

  private implicit val logger: Logger = Logger[TwitterImageClient]

  def post(image: WikimediaObject): Future[Tweet] = {
    val stream = new ByteArrayInputStream(image.image)

    for {
      mediaDetails <-
        log(() => tweetClient.uploadMediaFromInputStream(stream, image.image.length, image.mediaType), "image upload")
      tweet <- log(() => tweetClient.createTweet(status = image.name.take(280), media_ids = List(mediaDetails.media_id)), "image tweet")
        .andThen {
          case Success(tweet) => logger.info(s"Tweet at ${url(tweet)}")
        }
    } yield tweet
  }

  def deleteTweet(tweet: Tweet): Future[Tweet] =
    tweetClient.deleteTweet(tweet.id)

}
