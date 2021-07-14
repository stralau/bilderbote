package stralau.bilderbote.domain

import com.typesafe.scalalogging.Logger
import org.jsoup.Jsoup

import java.net.URLDecoder
import java.util.Locale
import scala.util.Try

trait StringAttribute {

  implicit val logger: Logger = Logger[StringAttribute]

  def source: String

  def take(n: Int): String = toString.take(n)

  override def toString: String = clean

  private def clean: String = stripHtml(urlDecode(source))

  private def urlDecode(s: String): String = Try(URLDecoder.decode(s, "utf-8")).recover(_ => s).get

  private def stripHtml(s: String): String = Jsoup.parse(s).text()

}

case class Image(uri: String)

case class Name(source: String) extends StringAttribute {

  override def toString: String = stripSuffix(super.toString)

  def stripSuffix(s: String): String = {
    val stripped = List("jpeg", "jpg", "png", "gif")
      .find(s.toLowerCase(Locale.US).endsWith)
      .map(suffix => ("(?i)\\." + suffix + "$").r)
      .map(_.replaceFirstIn(s, ""))
      .getOrElse(s)
    logger.info(s"Stripped: $stripped")
    stripped
  }

}

case class Author(source: String) extends StringAttribute

case class Licence(source: String) extends StringAttribute

case class Date(source: String) extends StringAttribute