package com.github.pjfanning.sourcedist

import sbt.{Artifact, File, SettingKey, TaskKey, settingKey, taskKey}

trait SourceDistKeys {
  lazy val sourceDistHomeDir: SettingKey[File] =
    settingKey[File]("Home directory which contains the projects sources, defaults to project root directory")
  lazy val sourceDistTargetDir: SettingKey[File] = settingKey[File](
    "Target directory where to create the archives, defaults dist folder under root project target folder"
  )
  lazy val sourceDistVersion: SettingKey[String] =
    settingKey[String]("The version to be used in the output archive file names, defaults to the root projects version")
  lazy val sourceDistName: SettingKey[String] = settingKey[String](
    "The name to be used as the prefix in the output archive file names, defaults to root projects name"
  )
  lazy val sourceDistIncubating: SettingKey[Boolean] = settingKey[Boolean](
    "Should 'incubating' appear in the output archive file names (required by Apache Incubator podlings)"
  )
  lazy val sourceDistSuffix: SettingKey[String] = settingKey[String](
    "The suffix to be used in the output archive file names, defaults to today's date in YYMMDD format"
  )
  lazy val signedSourceDistGenerate: TaskKey[Option[SignedGeneratedDist]] = taskKey[Option[SignedGeneratedDist]](
    "Signs the source distribution and provides a mapping of files their respective detached signatures"
  )
  lazy val sourceDistGenerate: TaskKey[GeneratedDist] =
    taskKey[GeneratedDist]("Generate the source distribution packages")
}
