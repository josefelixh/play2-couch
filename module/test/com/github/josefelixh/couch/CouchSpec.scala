package com.github.josefelixh.couch

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.matchers.MustMatchers
import com.ning.http.client.Realm
import org.mockito.Matchers
import play.api.http.{DefaultWriteables, ContentTypeOf, Writeable}
import play.api.libs.Codecs
import play.api.mvc.Codec

class CouchSpec extends FlatSpec with MustMatchers with MockitoSugar {

  "A Couch instance" should "build requests according to configuration" in new Couch {
    override val config = CouchConfig(
      couchInstanceUrl =  "http://couchurl.com",
      dbName =  "badger",
      credentials = Some(("mushroom", "snake"))
    )

    val request = db("/path")
    request.url must be === "http://couchurl.com/badger/path"
    request.headers must be === Map("Accept" -> List("application/json"), "Content-Type" -> List("application/json"))
    request.auth.get must be === ("mushroom", "snake", Realm.AuthScheme.BASIC)
    request.queryString must be === Map()
  }

  it should "use GET http method when calling info" in new CouchMock {
    this.info
    verify(requestMock).get()
  }

  it should "use PUT http method with empty body when calling create" in new CouchMock {
    this.create
    verify(requestMock).put(Matchers.eq("{}"))(Matchers.eq(wrt), Matchers.eq(cto))
  }

  it should "use DELETE http method when calling delete" in new CouchMock {
    this.delete
    verify(requestMock).delete()
  }

  it should "use GET http method and the right url when getting all docs" in new CouchMock {

    this.documents

    verify(requestMock).get()
    verifyNoMoreInteractions(requestMock)

    requestMock.url must be === config.couchInstanceUrl +  "/dbname/_all_docs"
  }

  it should "use GET http method and the right url when getting all dbs" in new CouchMock {

    this.databases

    verify(requestMock).get()
    verifyNoMoreInteractions(requestMock)

    requestMock.url must be === config.couchInstanceUrl +  "/_all_dbs"
  }

  import org.scalatest.Tag
  object FunctionalTest extends Tag("tags.FunctionalTest")
  it should "work end to end" taggedAs(FunctionalTest) in {
    val credentials = Some(("username", "password"))
    val couchConfig: CouchConfig = CouchConfig("https://username.cloudant.com", "heroku", credentials)
    implicit val couch = new Couch {
      val config: CouchConfig = couchConfig
    }


  }
}
