package stralau.bilderbote

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.Util.{essentials, readEnv, url}

import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.time.{Instant, LocalDate, ZoneId, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Random, Success}

object TwitterRetweetClient {

  val logger: Logger = Logger[TwitterAttributionClient]

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

  class TwitterRetweetClient(tweetClient: TwitterRestClient, imageAccountName: String) {

    def retweetRandom(): Future[Tweet] = tweetClient
      .userTimelineForUser(imageAccountName)
      .flatMap {
        rd => {
          val tweets = fromYesterday(rd.data)
          logger.info("Retweeting random tweet from: " + tweets.map(essentials))
          val randomTweet = tweets(Random.nextInt(tweets.length))
          logger.info("Retweeting tweet: " + essentials(randomTweet))
          tweetClient.retweet(randomTweet.id).andThen {
            case Success(tweet) => logger.info(s"Tweet at ${url(tweet)}")
          }
        }
    }
  }

  private def fromYesterday(tweets: Seq[Tweet]) = {
    val todayMidnight = LocalDate.now(ZoneId.of("Z")).atStartOfDay().toInstant(ZoneOffset.of("Z"))
    val yesterdayMidnight = todayMidnight.minus(24, ChronoUnit.HOURS)
    tweets
      .filter(_.created_at.isAfter(yesterdayMidnight))
      .filter(_.created_at.isBefore(todayMidnight))
  }

}
