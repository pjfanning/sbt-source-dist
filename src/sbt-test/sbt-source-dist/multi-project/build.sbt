name             := "project"
scalaVersion     := "2.13.10"
version          := "0.1.9"
sourceDistName   := "apache-pekko"
sourceDistSuffix := "20230331"

lazy val subOne = Project(id = "sub", file("sub")).settings(
  sourceDistSuffix := "20230331"
)
