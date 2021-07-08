package stralau.bilderbote

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.Util.{log, readEnv, url}
import stralau.bilderbote.domain.WikimediaObject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

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

}

class TwitterAttributionClient(twitterClient: TwitterRestClient, accountName: String) {

  implicit val logger: Logger = Logger[TwitterAttributionClient]

  def tweetAttribution(image: WikimediaObject, tweet: Tweet): Future[Tweet] = {
    val status = s"@$accountName " + List(
      s"Author: ${image.author.take(50)}",
      s"Date: ${image.date.take(30)}",
      s"Licence: ${image.licence.take(40)}",
      s"Source: ${image.url}"
    ).mkString("\n")

    log(() => twitterClient.createTweet(status, in_reply_to_status_id = Some(tweet.id)), "tweet attribution")
      .andThen {
        case Success(tweet) =>
          logger.info(s"Attribution at: ${url(tweet)}")
      }
  }

}
