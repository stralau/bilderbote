package stralau.bilderbote

import akka.http.scaladsl.model.{ErrorInfo, MediaType}
import com.typesafe.scalalogging.Logger
import stralau.bilderbote.domain.{Author, Date, Licence, Name, WikimediaObject}
import sttp.client3._

import scala.xml._

object WikimediaClient {
  def apply() = new WikimediaClient
}

class WikimediaClient {

  private val logger = Logger[WikimediaClient]

  private val backend = HttpURLConnectionBackend()

  private val basicRequest = sttp.client3.basicRequest
    .header("User-Agent", "Bilderbote: https://twitter.com/bilderbote")


  def getMetadata(location: String): Either[String, WikimediaObject] = {
    val xmlDesc = fetchXmlDesc(location)
    val imageLocation = (xmlDesc \ "file" \ "urls" \ "file").text
    val name = Name((xmlDesc \ "file" \ "name").text)
    val author = Author((xmlDesc \ "file" \ "author").text)
    val licence = Licence((xmlDesc \ "licenses" \ "license" \ "name").headOption.map(_.text).getOrElse(""))
    val date = Date((xmlDesc \ "file" \ "date").text)
    val url = (xmlDesc \ "file" \ "urls" \ "description").text
    fetchImage(imageLocation).map { case (mt, body) =>
      logger.info(s"Fetched image $name with media type $mt")
      WikimediaObject(mt, body, name, author, licence, date, url)
    }
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

