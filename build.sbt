import org.typelevel.sbt.gha.JavaSpec.Distribution.Temurin

name         := "sbt-source-dist"
organization := "com.github.pjfanning"
description  := "sbt plugin to generate source distributions"

sbtPlugin := true

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
scalacOptions ++= Seq(
  "-opt:l:inline",
  "-opt-inline-from:<sources>"
)

ThisBuild / scalaVersion := "2.12.18"

libraryDependencies ++= Seq(
  "org.eclipse.jgit"   % "org.eclipse.jgit" % "5.13.2.202306221912-r",
  "org.apache.commons" % "commons-compress" % "1.24.0",
  "org.scalatest"     %% "scalatest"        % "3.2.17" % Test
)

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")

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

ThisBuild / tlSonatypeUseLegacyHost    := true
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec(Temurin, "8"))
ThisBuild / githubWorkflowBuild        := Seq(WorkflowStep.Sbt(List("test")))
ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(name = Some("Build project"), commands = List("test", "scripted"))
)

ThisBuild / githubWorkflowBuildPreamble := Seq(
  WorkflowStep.Run(
    commands = List("gpg --import test-key.gpg"),
    name = Some("Setup key")
  )
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

enablePlugins(SbtPlugin)

scriptedLaunchOpts += ("-Dplugin.version=" + version.value)

scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}

scriptedBufferLog := false
