package stralau.bilderbote

import akka.http.scaladsl.model.{ErrorInfo, MediaType}
import com.typesafe.scalalogging.Logger
import org.jsoup.Jsoup
import sttp.client3._

import java.net.URLDecoder
import scala.language.postfixOps
import scala.util.Try
import scala.xml._

object WikimediaClient {
  def apply() = new WikimediaClient
}

class WikimediaClient {

  private val logger = Logger[WikimediaClient]

  private val backend = HttpURLConnectionBackend()

  def getMetadata(location: String): Either[String, WikimediaObject] = {
    val xmlDesc = fetchXmlDesc(location)
    val imageLocation = (xmlDesc \ "file" \ "urls" \ "file").text
    val name = clean((xmlDesc \ "file" \ "name").text)
    val author = clean((xmlDesc \ "file" \ "author").text)
    val license = (xmlDesc \ "licenses" \ "license" \ "name").headOption.map(_.text).getOrElse("")
    val url = (xmlDesc \ "file" \ "urls" \ "description").text
    fetchImage(imageLocation).map { case (mt, body) =>
      logger.info(s"Fetched image $name with media type $mt")
      WikimediaObject(mt, body, name, author, license, url)
    }
  }

  private def clean(s: String): String = {
    logger.info(s"raw string: $s")
    val decoded = Try(URLDecoder.decode(s, "utf-8")).recover(_ => s).get
    val clean = Jsoup.parse(decoded).text()
    logger.info(s"cleaned up string: $clean")
    clean
  }

  def fetchRandomFileLocation: String = {
    basicRequest
      .get(uri"https://commons.wikimedia.org/wiki/Special:Random/File")
      .followRedirects(false)
      .send(backend)
      .header("Location")
      .get
  }

  private def fetchXmlDesc(location: String) = {
    val image = fileName(location)

    val response = basicRequest
      .get(uri"https://magnus-toolserver.toolforge.org/commonsapi.php?image=$image")
      .send(backend)
      .body
      .toOption
      .get

    XML.load(Source.fromString(response))
  }

  private def fileName(location: String) = {
    location.split("/").last.replaceFirst("^File:", "")
  }

  private def fetchImage(imageLocation: String): Either[String, (MediaType, Array[Byte])] = {
    val response = basicRequest
      .get(uri"$imageLocation")
      .response(asByteArray)
      .send(backend)

    mediaType(response).flatMap(mt =>
      response.body.map(body => (mt, body))
    )

  }

  private def mediaType[T](response: Response[T]) =
    response.header("Content-Type").map(parseContentType) match {
      case Some(e) => e
      case None => Left("Missing Content-Type header")
    }

  private def parseContentType(contentType: String) =
    MediaType.parse(contentType).left.map(errorToString)

  private def errorToString(errors: List[ErrorInfo]): String = errors.map(_.summary).mkString(", ")

}

case class Image(uri: String)

case class WikimediaObject(
  mediaType: MediaType,
  image: Array[Byte],
  name: String,
  author: String,
  licence: String,
  url: String
)