package controllers

import play.api._
import play.api.mvc._
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import scala.Some
import scala.util.{Failure, Success}
import com.github.josefelixh.couch._
import com.github.josefelixh.couch.CouchDocument._

object Application extends Controller {

  case class Role(name: String, permissions: Int)
  case class Profile(id: String, level: Int, roles: Vector[Role])

  val role = Role("admin", 777)
  val profile = Profile("badger", 0, Vector(role))

  val credentials = Some(("josefelixh", "cloudant123"))
  val couchConfig: CouchConfig = CouchConfig("https://josefelixh.cloudant.com", "heroku", credentials)

  implicit val executionContext = ExecutionContext.Implicits.global
  implicit val couch = Couch(couchConfig)
  implicit val roleFormat = Json.format[Role]
  implicit val profileFormat = Json.format[Profile]

  def index = Action { Ok("Ok")}

  def index2 = Action {
    val future = for {
      createdNoId <- CouchDocument(profile).create
      created <- CouchDocument("PROFILE_ID", profile).create
      docId = Id[Profile](createdNoId.id.get)
      retreived <- docId.retrieve
      updated <- createdNoId.update(current => current.copy(level = 1))
      updateRetreived <- docId.retrieve
      deleteResponse <- updated.delete
      deleteResponse2 <- created.delete
    } yield {
      s"CREATED : $createdNoId" ::
        s"CREATED : $created" ::
        s"RETREIVED : $retreived" ::
        s"UPDATED : $updated" ::
        s"RETREIVED : $updateRetreived" ::
        s"DELETED : $deleteResponse2" ::
        Nil
    }
    Async {
      future map ( operationsList => Ok(operationsList.mkString("\n")))
    }

  }

}