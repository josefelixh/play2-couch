package com.github.josefelixh.couch

import com.ning.http.client.Realm.AuthScheme._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.ws._
import play.api.http.{ContentTypeOf, DefaultWriteables, Writeable}
import play.api.mvc.Codec


case class CouchConfig(
  couchInstanceUrl: String,
  dbName: String,
  credentials: Option[(String,String)] = None
)

object Couch {
  def apply(couchConfig: CouchConfig): Couch = new Couch {
    override val config: CouchConfig = couchConfig
  }
}
trait Couch {
  val config: CouchConfig
  private lazy val couchUrl = config.couchInstanceUrl
  private lazy val dbName = config.dbName
  private lazy val credentials = config.credentials

  private[couch] def couch(path: String): WSRequestHolder = {
    val ws = WS.url(s"$couchUrl$path")
      .withHeaders(
        "Accept" -> "application/json",
        "Content-Type" -> "application/json"
      )

    credentials.map { case (username: String, password: String) =>
      ws.withAuth(username, password, BASIC)
    } getOrElse ws

  }


  private[couch] def db(path: String): WSRequestHolder = couch(s"/$dbName$path")

  val wrt: Writeable[String] = new DefaultWriteables{}.wString(Codec.utf_8)
  val cto: ContentTypeOf[String] = new ContentTypeOf[String](Some("application/json; charset=utf-8"))

  def info = db("/").get()
  def create = db("/").put("{}")(wrt, cto)
  def delete = db("/").delete()
  def documents = db("/_all_docs").get()
  def databases = couch("/_all_dbs").get()

  def create[T](docs: Seq[CouchDocument[T]])(implicit fmt: Format[T], exec: ExecutionContext) = {
    val json = docs.foldLeft(Json.arr()) { (json, doc) => {
      json :+ (doc.id match {
          case Some(id) => Json.toJson(doc.doc.get).transform(JSONTransformers.AddCouchId(doc.id)).get
          case None => Json.toJson(doc.doc.get)
        })
      }
    }

    db("/_bulk_docs").post(Json.obj("docs" -> json)) map { response =>
//      val zipped = (response.json \ "docs" \\ "id") zip (response.json \ "docs" \\ "rev") zip docs
//      zipped map { case ((id, rev), doc) =>
//        new CouchDocument[T](Some(id.validate[String].get), Some(rev.validate[String].get), Some(doc))
//      }
//      ((response.json  \\ "id").map(_.validate[String].get) zip docs) map {
//        case (id, doc) => CouchDocument(id, doc)()
//      }
//      response.json.toString()
    }
  }
}
