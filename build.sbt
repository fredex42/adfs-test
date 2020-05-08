import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
import play.sbt.PlayImport

name := "adfs_test"
 
version := "1.0"

enablePlugins(DockerSpotifyClientPlugin)

lazy val `adfs_test` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

//needed for OpenSAML which is required by pac4j-saml
resolvers += "Mulesoft Repository" at "https://repository.mulesoft.org/nexus/content/repositories/public/"
resolvers += "Shibboleth Repository" at "https://build.shibboleth.net/nexus/content/repositories/releases/"

scalaVersion := "2.13.2"

libraryDependencies ++= Seq( jdbc , ws , specs2 % Test , guice )

unmanagedResourceDirectories in Test +=  (baseDirectory ( _ /"target/web/public/test" )).value

val circeVersion = "0.13.0"
//pac4j 4.0.0 release has a dodgy dependency on a SNAPSHOT release that seems to have disappeared. RC3 doesn't.
libraryDependencies ++= Seq(
  "com.github.karelcemus" %% "play-redis" % "2.6.0",
  // runtime DI
  PlayImport.guice,
  // runtime DI
  PlayImport.cacheApi,
  "org.pac4j" %% "play-pac4j" % "10.0.0",
  "org.pac4j" % "pac4j-saml" % "4.0.0-RC3",
  "org.pac4j" % "pac4j-http" % "4.0.0-RC3",
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