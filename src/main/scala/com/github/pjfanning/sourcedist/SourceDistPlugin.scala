package com.github.pjfanning.sourcedist

import sbt._
import Keys._
import com.github.sbt.git.SbtGit.git.gitUncommittedChanges
import sbt.{AutoPlugin, Setting}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SourceDistPlugin extends AutoPlugin {
  object autoImport extends SourceDistKeys

  import autoImport._

  def sourceDistGlobalSettings: Seq[Setting[_]] = Seq(
    sourceDistHomeDir   := baseDirectory.value,
    sourceDistTargetDir := target.value / "dist",
    sourceDistVersion   := version.value,
    sourceDistName      := name.value,
    sourceDistSuffix    := LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
    sourceDistGenerate := SourceDistGenerate.generateSourceDists(
      homeDir = sourceDistHomeDir.value.getAbsolutePath,
      prefix = sourceDistName.value,
      version = sourceDistVersion.value,
      suffix = sourceDistSuffix.value,
      targetDir = sourceDistTargetDir.value.getAbsolutePath,
      logger = streams.value.log,
      gitUncommittedChanges = gitUncommittedChanges.value
    )
  )

  override def projectSettings: Seq[Setting[_]] = sourceDistGlobalSettings

  override def trigger = noTrigger

}
