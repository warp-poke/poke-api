name := """poke-api"""
organization := "com.clever-cloud"

version := "1.0-SNAPSHOT"



lazy val libAnormEntities = RootProject(uri("ssh://git@github.com/divarvel/anorm-pg-entity.git#magnolia"))

lazy val root = (project in file(".")).enablePlugins(PlayScala).dependsOn(libAnormEntities)

scalaVersion := "2.11.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "org.postgresql" % "postgresql" % "42.1.4",
  "io.warp10" % "token" % "1.0.10-29-gd8b6b0d"
)

libraryDependencies += "com.github.tyagihas" % "scala_nats_2.11" % "0.3.0"

libraryDependencies += jdbc
libraryDependencies += evolutions

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.clever-cloud.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.clever-cloud.binders._"


resolvers += "cityzendata-bintray" at "http://dl.bintray.com/cityzendata/maven"
resolvers += "hbs-bintray" at "http://dl.bintray.com/hbs/maven"
