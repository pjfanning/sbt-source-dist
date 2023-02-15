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
    LocalRootProject / sourceDistHomeDir   := (LocalRootProject / baseDirectory).value,
    LocalRootProject / sourceDistTargetDir := (LocalRootProject / target).value / "dist",
    LocalRootProject / sourceDistVersion   := (LocalRootProject / version).value,
    LocalRootProject / sourceDistName      := (LocalRootProject / name).value,
    LocalRootProject / sourceDistSuffix    := LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
    LocalRootProject / sourceDistGenerate := SourceDistGenerate.generateSourceDists(
      homeDir = (LocalRootProject / sourceDistHomeDir).value.getAbsolutePath,
      prefix = (LocalRootProject / sourceDistName).value,
      version = (LocalRootProject / sourceDistVersion).value,
      suffix = (LocalRootProject / sourceDistSuffix).value,
      targetDir = (LocalRootProject / sourceDistTargetDir).value.getAbsolutePath,
      logger = streams.value.log
    )
  )

  override lazy val buildSettings: Seq[Setting[_]] = sourceDistSettings

  override def trigger = allRequirements

}
