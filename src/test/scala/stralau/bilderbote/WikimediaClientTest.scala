package stralau.bilderbote

import org.scalatest.funsuite._

class WikimediaClientTest extends AsyncFunSuite {

  private val wikiClient: WikimediaClient = WikimediaClient()
  private val twitterClient = TwitterImageClient()

}
