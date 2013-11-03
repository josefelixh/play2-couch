package com.github.josefelixh.couch

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import scala.concurrent.ExecutionContext
import play.api.libs.ws.WS.WSRequestHolder

trait CouchMock extends Couch with MockitoSugar {
  implicit val couch = this
  implicit val executionContext = ExecutionContext.Implicits.global

  override val config = CouchConfig("http://couchurl.test", "dbname")

  val requestMock = {
    val requestMock = mock[WSRequestHolder]
    stub(requestMock.withQueryString(anyObject())).toReturn(requestMock)
    stub(requestMock.withHeaders(anyObject())).toReturn(requestMock)
    requestMock
  }

  override def couch(path: String): WSRequestHolder = {
    stub(requestMock.url).toReturn(config.couchInstanceUrl+path)
    requestMock
  }

  override def db(path: String): WSRequestHolder = {
    stub(requestMock.url).toReturn(baseUrlMock+path)
    requestMock
  }

  lazy val baseUrlMock = s"${config.couchInstanceUrl}/${config.dbName}"
}
