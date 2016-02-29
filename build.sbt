import play.PlayScala

name := "play-scala"

version := "1.0-SNAPSHOT"


// Declare MySQL connector Dependency

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.12"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

