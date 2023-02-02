package com.github.pjfanning.sourcedist

import sbt._
import Keys._
import sbt.{AutoPlugin, Setting}

object SourceDistPlugin extends AutoPlugin {
  object autoImport extends SourceDistKeys

  import autoImport._

  def sourceDistGlobalSettings: Seq[Setting[_]] = Seq(
    sourceDistHomeDir := baseDirectory.value,
    sourceDistTargetDir := target.value / "dist",
    sourceDistVersion := version.value,
    sourceDistName := name.value,
    sourceDistGenerate := SourceDistGenerate.generateSourceDists(
      sourceDistHomeDir.value.getAbsolutePath,
      sourceDistName.value,
      sourceDistVersion.value,
      sourceDistTargetDir.value.getAbsolutePath)
  )

  override def projectSettings: Seq[Setting[_]] = sourceDistGlobalSettings

  override def trigger = noTrigger

}