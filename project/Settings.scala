import sbt.Keys._
import sbt._

object Settings {
  lazy val commonSettings = Seq(
    organization := "com.github.BambooTuna",
    publishTo := Some(Resolver.file("WebSocketManager",file("."))(Patterns(true, Resolver.mavenStyleBasePattern))),
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(
      Circe.core,
      Circe.generic,
      Circe.parser,
      Akka.http,
      Akka.stream,
      Akka.slf4j,
      Enumeratum.version,
      Logback.classic,
      LogstashLogbackEncoder.encoder
    )
  )

}
