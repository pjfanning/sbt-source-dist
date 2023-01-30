ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

name := "source-dist"

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.11.0",
  "org.apache.commons" % "commons-compress" % "1.22"
)
