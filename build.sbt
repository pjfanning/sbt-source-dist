/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

name         := "sbt-source-dist"
organization := "com.github.pjfanning"
description  := "sbt plugin to generate source distributions"

sbtPlugin := true

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

// compile settings
scalacOptions ++=
  List(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "UTF-8"
  ) ++ {
    scalaBinaryVersion.value match {
      case "2.12" => List("-language:_")
      case _      => List.empty
    }
  }

scalacOptions ++= {
  if (insideCI.value) {
    val log = sLog.value
    log.info("Running in CI, enabling Scala2 optimizer")
    Seq(
      "-opt-inline-from:<sources>",
      "-opt:l:inline"
    )
  } else Nil
}

val scala212 = "2.12.21"
val scala3 = "3.8.2"
ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Seq(scala212)
ThisBuild / crossScalaVersions := Seq(scala212, scala3)

def sbtVersionForPlugin(scalaBinary: String): String =
  scalaBinary match {
    case "2.12" => "1.12.9"
    case _      => "2.0.0-RC11"
  }

libraryDependencies ++= Seq(
  "org.eclipse.jgit"   % "org.eclipse.jgit" % "5.13.5.202508271544-r",
  "org.apache.commons" % "commons-compress" % "1.28.0",
  "org.scalatest"     %% "scalatest"        % "3.2.20" % Test
)

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0")

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

(pluginCrossBuild / sbtVersion) := sbtVersionForPlugin(scalaBinaryVersion.value)

enablePlugins(SbtPlugin)

scriptedLaunchOpts += ("-Dplugin.version=" + version.value)

scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}

scriptedBufferLog := false
scriptedSbt := sbtVersionForPlugin(scalaBinaryVersion.value)

ThisBuild / githubWorkflowOSes := Seq("ubuntu-latest", "macos-latest", "windows-latest")

ThisBuild / githubWorkflowJavaVersions := Seq(
  // Java 17 first: publish job uses the head of this list when downloading staged artifacts; sbt 2 (Scala 3 axis) needs 17+.
  JavaSpec.temurin("17"),
  JavaSpec.temurin("8")
)

ThisBuild / githubWorkflowBuildMatrixExclusions ++= Seq(
  MatrixExclude(Map("java" -> "temurin@8", "os" -> "macos-latest")),
  MatrixExclude(Map("scala" -> scala3, "java" -> "temurin@8"))
)

ThisBuild / githubWorkflowScalaVersions := Seq(scala212, scala3)

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(
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
