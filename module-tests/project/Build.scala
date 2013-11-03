import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "module-tests"
  val appVersion      = "1.0-SNAPSHOT"

  val josefelixhRepos= Seq(
    "josefelixh repository snapshots" at "http://josefelixh.github.com/repo/SNAPSHOTS",
    "josefelixh repository releases" at "http://josefelixh.github.com/repo/RELEASES"
  )

  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.github.josefelixh" %% "play2-couch" % "1.0-SNAPSHOT"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers ++= josefelixhRepos
  )

}
