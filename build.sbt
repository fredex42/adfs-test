import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
import play.sbt.PlayImport

name := "adfs_test"
 
version := "1.0"

enablePlugins(DockerSpotifyClientPlugin)

lazy val `adfs_test` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.13.2"

libraryDependencies ++= Seq( jdbc , ws , specs2 % Test , guice )

unmanagedResourceDirectories in Test +=  (baseDirectory ( _ /"target/web/public/test" )).value

val circeVersion = "0.13.0"
libraryDependencies ++= Seq(
  "com.nimbusds" % "nimbus-jose-jwt" % "8.17",
  "com.dripower" %% "play-circe" % "2812.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
)

packageName in Docker := "adfs_test_scala"
version in Docker := "latest"
dockerBaseImage := "openjdk:11.0.7-slim-buster"
dockerExposedPorts := Seq(9000)
dockerUsername := Some("andyg42")
dockerPermissionStrategy := DockerPermissionStrategy.Run