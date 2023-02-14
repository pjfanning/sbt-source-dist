package com.github.pjfanning.sourcedist

import sbt._
import Keys._
import sbt.{AutoPlugin, Setting}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SourceDistPlugin extends AutoPlugin {
  object autoImport extends SourceDistKeys

  import autoImport._

  private[sourcedist] lazy val sourceDistSettings: Seq[Setting[_]] = Seq(
    sourceDistHomeDir   := (LocalRootProject / baseDirectory).value,
    sourceDistTargetDir := (LocalRootProject / target).value / "dist",
    sourceDistVersion   := (LocalRootProject / version).value,
    sourceDistName      := (LocalRootProject / name).value,
    sourceDistSuffix    := LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
    sourceDistGenerate := SourceDistGenerate.generateSourceDists(
      homeDir = sourceDistHomeDir.value.getAbsolutePath,
      prefix = sourceDistName.value,
      version = sourceDistVersion.value,
      suffix = sourceDistSuffix.value,
      targetDir = sourceDistTargetDir.value.getAbsolutePath,
      logger = streams.value.log
    )
  )

  override lazy val projectSettings: Seq[Setting[_]] = sourceDistSettings

  override def trigger = allRequirements

}
