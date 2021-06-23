name := "bilderbote"

version := "1.0"

scalaVersion := "2.13.1"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "com.danielasfregola" %% "twitter4s" % "6.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3",

  "com.softwaremill.sttp.client3" %% "core" % "3.3.6",
  "org.scala-lang.modules" %% "scala-xml" % "2.0.0",
  "org.jsoup" % "jsoup" % "1.13.1",

  "org.scalactic" %% "scalactic" % "3.2.9",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test"
)
