ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val CatsCoreVersion = "2.8.0"
val CatsEffectVersion = "3.3.12"
val ScalaParallelCollectionsVersion = "1.0.4"
val ScalaCSVVersion = "1.3.10"
val AkkaVersion = "2.6.19"
val ScalaTestVersion = "3.2.12"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % CatsCoreVersion,
  "org.typelevel" %% "cats-effect" % CatsEffectVersion,
  "org.scala-lang.modules" %% "scala-parallel-collections" % ScalaParallelCollectionsVersion,
  "com.github.tototoshi" %% "scala-csv" % ScalaCSVVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "humidity-sensor-accenture"
  )
