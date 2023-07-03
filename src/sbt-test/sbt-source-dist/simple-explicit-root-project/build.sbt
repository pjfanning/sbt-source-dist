val root = Project(id = "root", base = file(".")).settings(
  scalaVersion     := "2.13.10",
  version          := "0.1.9",
  sourceDistName   := "apache-pekko",
  sourceDistSuffix := "20230331"
)
