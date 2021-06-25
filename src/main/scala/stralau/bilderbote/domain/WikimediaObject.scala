package stralau.bilderbote.domain

import akka.http.scaladsl.model.MediaType
import stralau.bilderbote.BilderBote.twitterMaxImageSize
import stralau.bilderbote.TwitterImageClient.knownMediaTypes
import stralau.bilderbote.domain.WikimediaObject.{validateMediaType, validateSize}

import java.io.ByteArrayInputStream

object WikimediaObject {
  private def validateSize(image: WikimediaObject): Either[String, WikimediaObject] =
    if (image.image.length <= twitterMaxImageSize) Right(image)
    else Left("Image size too large")

  private def validateMediaType(image: WikimediaObject): Either[String, WikimediaObject] = {
    if (knownMediaTypes.contains(image.mediaType)) Right(image)
    else Left("Wrong media type")
  }
}

case class WikimediaObject(
  mediaType: MediaType,
  image: Array[Byte],
  name: Name,
  author: Author,
  licence: Licence,
  url: String
) {

  def validate: Either[String, WikimediaObject] =
    validateMediaType(this).flatMap(validateSize)

  def stream: ByteArrayInputStream = new ByteArrayInputStream(image)
}

