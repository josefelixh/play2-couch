package com.github.josefelixh.couch

import play.api.libs.functional._
import play.api.libs.json._
import scala.concurrent._
import play.api.http.Writeable._
import play.api.http.ContentTypeOf._
import play.api.http.{DefaultWriteables, ContentTypeOf, Writeable}

object CouchDocument {

  implicit def toDocument[T](cd: CouchDocument[T]): T = cd.doc.get

  def Id[T](id: String)(implicit fmt: Format[T]): CouchDocument[T] = {
    val doc: Option[T] = None
    CouchDocument(Some(id), None, doc)
  }

  def apply[T](t: T)(implicit couch: Couch,  fmt: Format[T], execCtx: ExecutionContext): CouchDocument[T] =
    CouchDocument[T](None, None, Some(t))

  def apply[T](id: String, t: T)(implicit couch: Couch,  fmt: Format[T], execCtx: ExecutionContext): CouchDocument[T] =
    CouchDocument[T](Some(id), None, Some(t))

  def create_:[T](t: T)(implicit couch: Couch,  fmt: Format[T]) = {
    CouchDocument[T](None, None, Some(t))
  }

  val wrt: Writeable[JsValue] = new DefaultWriteables {}.writeableOf_JsValue
  val cto: ContentTypeOf[JsValue] = new ContentTypeOf[JsValue](Some("application/json; charset=utf-8"))

}

case class CouchDocument[T](id: Option[String] = None, rev: Option[String] = None, doc: Option[T])(implicit fmt: Format[T])
  extends JSONTransformers[T] with ResponseTransformers[T] {

  def create(implicit couch: Couch, execCtx: ExecutionContext): Future[CouchDocument[T]] = {
    val jsDoc = this.doc match {
      case Some(d) => Json.toJson(d).transform(AddCouchIdToJson).asOpt.get
      case None => Json.parse("{}")
    }
    val response = couch.db("/").post(jsDoc)(CouchDocument.wrt, CouchDocument.cto)
    response map { IdAndRevision }
  }

  def retrieve(implicit couch: Couch, execCtx: ExecutionContext): Future[CouchDocument[T]] = {
    val response = couch.db(s"/${id.get}").get()
    response map { implicit r =>  RevisionAndDocument }
  }

  def delete(implicit couch: Couch, execCtx: ExecutionContext): Future[CouchDocument[T]] = {
    val requestHolder = couch.db(s"/${id.get}").withQueryString("rev" -> rev.get)

    for ( response <- requestHolder.delete() ) yield {
      if ((response.json \ "ok").validate[Boolean].getOrElse(false))
        this.copy(id = None,rev = None)
      else
        this
    }
  }

  def update(updator: T => T)(implicit couch: Couch, execCtx: ExecutionContext): Future[CouchDocument[T]] = {
    val updatedDoc = updator(this.doc.get)
    val updatedDocJson = Json.toJson(updatedDoc)
    val requestHolder = couch.db(s"/${id.get}").withQueryString(
      "rev" -> rev.get
    ).withHeaders(
      "If-Match" -> rev.get
    )
    val response = requestHolder.put(updatedDocJson.toString())(couch.wrt, couch.cto)
    response map { Revision(updatedDoc)(_, fmt) }
  }

  def withId(x: String) = this.copy(id = Some(x))
  def withRev(x: String) = this.copy(rev = Some(x))
  def withDoc[A](doc: A)(implicit fmt: Format[A]) = new CouchDocument(this.id, this.rev, Some(doc))(fmt)

  override def toString = {
    s"CouchDocument(_id = ${id.getOrElse(None)}, _rev = ${rev.getOrElse(None)}, ${doc.getOrElse(None)}"
  }
}