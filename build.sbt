import org.typelevel.sbt.gha.JavaSpec.Distribution.Zulu

name         := "sbt-source-dist"
organization := "com.github.pjfanning"
description  := "sbt plugin to generate source distributions"

sbtPlugin := true

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

ThisBuild / scalaVersion := "2.12.17"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-compress" % "1.22",
  "org.scalatest"     %% "scalatest"        % "3.2.15" % Test
)

homepage := Some(url("https://github.com/pjfanning/sbt-source-dist"))

licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))

developers := List(
  Developer(id = "pjfanning", name = "PJ Fanning", email = "", url = url("https://github.com/pjfanning")),
  Developer(id = "mdedetrich",
            name = "Matthew de Detrich",
            email = "mdedetrich@gmail.com",
            url = url("https://github.com/mdedetrich")
  )
)

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec(Zulu, "8"))
ThisBuild / githubWorkflowBuild        := Seq(WorkflowStep.Sbt(List("test")))
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE"      -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"          -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD"   -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME"   -> "${{ secrets.SONATYPE_USERNAME }}",
      "CI_SNAPSHOT_RELEASE" -> "+publishSigned"
    )
  )
)
