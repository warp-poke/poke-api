name := """poke-api"""
organization := "com.clever-cloud"

version := "1.0-SNAPSHOT"

lazy val libAnormEntities = RootProject(uri("https://github.com/divarvel/anorm-pg-entity.git#warp-poke"))

lazy val root = (project in file(".")).enablePlugins(PlayScala).dependsOn(libAnormEntities)

scalaVersion := "2.11.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// https://github.com/phxql/argon2-jvm
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "org.postgresql" % "postgresql" % "42.1.4",
  "io.warp10" % "token" % "1.0.10-29-gd8b6b0d",
  "com.github.nitram509" % "jmacaroons" % "0.3.1",
  "de.mkammerer" % "argon2-jvm" % "2.3",
  "net.cakesolutions" %% "scala-kafka-client-akka" % "1.0.0"
)

javaOptions += "-Djava.security.auth.login.config=${JAAS_PATH}"

javaOptions in Universal += "-Djava.security.auth.login.config=" + sys.env.get("JAAS_PATH").getOrElse("conf/kafkacreds.jaas")

libraryDependencies += jdbc
libraryDependencies += evolutions

// Adds additional packages into Twirl
// TwirlKeys.templateImports += "com.clever-cloud.controllers._"

// Adds additional packages into conf/routes
play.sbt.routes.RoutesKeys.routesImport += "models.entities.Service._"

resolvers += "cityzendata-bintray" at "http://dl.bintray.com/cityzendata/maven"
resolvers += "hbs-bintray" at "http://dl.bintray.com/hbs/maven"
resolvers += Resolver.bintrayRepo("cakesolutions", "maven")
