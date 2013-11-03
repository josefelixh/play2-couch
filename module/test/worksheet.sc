import com.github.josefelixh.couch.CouchConfig
import com.github.josefelixh.couch.{Couch, CouchConfig, CouchDocument}
import play.api.libs.json._
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.Some
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


val docs = List(CouchDocument(profile).withId("2"), CouchDocument(profile).withId("1"))




val created = docs map { _.create }


val future = CouchDocument(profile).withId("2").retrieve

future.map(cd => println(cd.toString))


import concurrent.duration._


Await.result(future, 1 second)



























