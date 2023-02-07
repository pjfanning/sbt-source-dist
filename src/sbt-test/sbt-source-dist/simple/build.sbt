lazy val root = (project in file("."))
  .enablePlugins(SourceDistPlugin)
  .settings(
    scalaVersion := "2.13.10",
    version := "0.1",
    sourceDistName := "incubator-pekko"
  )