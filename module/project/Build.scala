import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appOrganisation = "com.github.josefelixh"
  val appName         = "play2-couch"
  val appVersion      = "1.0-SNAPSHOT"

  val snapshotsRepo = "/home/jfh/github/josefelix.github.com/repo/SNAPSHOTS"
  val releasesRepo = "/home/jfh/github/josefelix.github.com/repo/RELEASES"

  val snapshotRegexp = "^.*SNAPSHOT$".r
  val thisVersionRepo = appVersion match {
    case snapshotRegexp() => Resolver.file("git repo", new File(snapshotsRepo))
    case _ => Resolver.file("git repo", new File(releasesRepo))
  }

  val appDependencies = Seq()

  val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "org.mockito" % "mockito-core" % "1.9.5" % "test"
  )

  val dependencies = appDependencies ++ testDependencies


  val main = play.Project(appName, appVersion, dependencies).settings(
    organization := appOrganisation,
    scalacOptions := Seq("-feature", "-language:implicitConversions"),
    publishTo := Some(thisVersionRepo)
  )

}
