package stralau.bilderbote

import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.Util.retry

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object BilderBoteEconomy {

  private implicit val logger: Logger = Logger("bilderbote_economy")

  private val twitterRetweetClient = TwitterRetweetClient()

  def main(args: Array[String]): Unit = run

  def run: Tweet =
    Await.result(retry(() => twitterRetweetClient.retweetRandom())(3), 2.minutes)

}
