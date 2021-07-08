package stralau.bilderbote

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.Util.{essentials, readEnv, url}

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, ZoneId, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Random, Success}
import scala.async.Async.{async, await}

object TwitterRetweetClient {

  def apply(): TwitterRetweetClient = {
    val consumerTokenKey = readEnv("RETWEET_TWITTER_CONSUMER_TOKEN_KEY")
    val consumerTokenSecret = readEnv("RETWEET_TWITTER_CONSUMER_TOKEN_SECRET")
    val accessTokenKey = readEnv("RETWEET_TWITTER_ACCESS_TOKEN_KEY")
    val accessTokenSecret = readEnv("RETWEET_TWITTER_ACCESS_TOKEN_SECRET")
    val consumerToken = ConsumerToken(key = consumerTokenKey, secret = consumerTokenSecret)
    val accessToken = AccessToken(key = accessTokenKey, secret = accessTokenSecret)
    val imageAccountName = readEnv("IMAGE_ACCOUNT_NAME")
    val restClient = TwitterRestClient(consumerToken, accessToken)

    new TwitterRetweetClient(restClient, imageAccountName)
  }
}

class TwitterRetweetClient(tweetClient: TwitterRestClient, imageAccountName: String) {

  val logger: Logger = Logger[TwitterAttributionClient]

  def retweetRandom(): Future[Option[Tweet]] = async {
    val timeline = await(tweetClient.userTimelineForUser(imageAccountName)).data
    val tweets = fromYesterday(timeline)
    logger.info("Retweeting random tweet from: " + tweets.map(essentials))
    val randomTweet = tweets(Random.nextInt(tweets.length))
    logger.info("Random tweet: " + essentials(randomTweet))

    await(filterRetweeted(randomTweet)) match {
      case Some(tweet) =>
        logger.info("Retweeting")
        Some(await(tweetClient.retweet(tweet.id).andThen {
          case Success(tweet) => logger.info(s"Tweet at ${url(tweet)}")
        }))
      case None =>
        logger.info("This was already retweeted – do nothing")
        None
    }
  }

  private def fromYesterday(tweets: Seq[Tweet]) = {
    val todayMidnight = LocalDate.now(ZoneId.of("Z")).atStartOfDay().toInstant(ZoneOffset.of("Z"))
    val yesterdayMidnight = todayMidnight.minus(24, ChronoUnit.HOURS)
    tweets
      .filter(_.created_at.isAfter(yesterdayMidnight))
      .filter(_.created_at.isBefore(todayMidnight))
  }

  private def filterRetweeted(tweet: Tweet): Future[Option[Tweet]] = {
    tweetClient.homeTimeline().map { rd =>
      Some(tweet).filterNot(t => rd.data.flatMap(_.retweeted_status).map(_.id).contains(t.id))
    }
  }
}

