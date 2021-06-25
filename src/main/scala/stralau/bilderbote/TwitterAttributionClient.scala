package stralau.bilderbote

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.Util.{log, url}
import stralau.bilderbote.domain.WikimediaObject

import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

object TwitterAttributionClient {

  val logger: Logger = Logger[TwitterAttributionClient]

  def apply(): TwitterAttributionClient = {
    val consumerTokenKey = readEnv("ATTRIBUTION_TWITTER_CONSUMER_TOKEN_KEY")
    val consumerTokenSecret = readEnv("ATTRIBUTION_TWITTER_CONSUMER_TOKEN_SECRET")
    val accessTokenKey = readEnv("ATTRIBUTION_TWITTER_ACCESS_TOKEN_KEY")
    val accessTokenSecret = readEnv("ATTRIBUTION_TWITTER_ACCESS_TOKEN_SECRET")
    val consumerToken = ConsumerToken(key = consumerTokenKey, secret = consumerTokenSecret)
    val accessToken = AccessToken(key = accessTokenKey, secret = accessTokenSecret)
    val accountName = readEnv("TWITTER_ACCOUNT_NAME")
    val restClient = TwitterRestClient(consumerToken, accessToken)

    new TwitterAttributionClient(restClient, accountName)
  }

  private def readEnv(key: String): String = {
    sys.env.get(key).map {
      value =>
//        logger.info(s"Env[$key] = $value")
        value
    }.getOrElse(throw new RuntimeException(s"Could not find env var $key"))
  }
}

class TwitterAttributionClient(twitterClient: TwitterRestClient, accountName: String) {

  implicit val logger: Logger = Logger[TwitterAttributionClient]

  def tweetAttribution(image: WikimediaObject, tweet: Tweet): Future[Tweet] = {
    val status = s"@$accountName " +
      List(s"Author: ${image.author}", s"Licence: ${image.licence}", s"Source: ${image.url}").mkString("\n")
    log(() => twitterClient.createTweet(status, in_reply_to_status_id = Some(tweet.id)), "tweet attribution")
      .andThen {
        case Success(tweet) =>
          logger.info(s"Attribution at: ${url(tweet)}")
      }
  }

}
