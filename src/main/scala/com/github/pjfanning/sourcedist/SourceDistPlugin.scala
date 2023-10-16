package com.github.pjfanning.sourcedist

import sbt._
import sbt.Keys._
import com.jsuereth.sbtpgp.PgpKeys.pgpSigner
import com.jsuereth.sbtpgp.{SbtPgp, gpgExtension}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SourceDistPlugin extends AutoPlugin {
  object autoImport extends SourceDistKeys

  import autoImport._

  private[sourcedist] lazy val sourceDistSettings: Seq[Setting[_]] = Seq(
    LocalRootProject / sourceDistHomeDir    := (LocalRootProject / baseDirectory).value,
    LocalRootProject / sourceDistTargetDir  := (LocalRootProject / target).value / "dist",
    LocalRootProject / sourceDistVersion    := (LocalRootProject / version).value,
    LocalRootProject / sourceDistName       := (LocalRootProject / name).value,
    LocalRootProject / sourceDistSuffix     := LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
    LocalRootProject / sourceDistIncubating := false,
    LocalRootProject / signedSourceDistGenerate := {
      val sourceDistGenerated = (LocalRootProject / sourceDistGenerate).value
      val r                   = pgpSigner.value
      val skipZ               = (pgpSigner / skip).value
      val s                   = streams.value
      if (!skipZ) {
        Some(
          sourceDistGenerated.toSignedGeneratedDist(
            r.sign(sourceDistGenerated.dist, new File(sourceDistGenerated.dist + gpgExtension), s)
          )
        )
      } else
        None
    },
    LocalRootProject / sourceDistGenerate := SourceDistGenerate.generateSourceDists(
      homeDir = (LocalRootProject / sourceDistHomeDir).value.getAbsolutePath,
      prefix = (LocalRootProject / sourceDistName).value,
      version = (LocalRootProject / sourceDistVersion).value,
      suffix = (LocalRootProject / sourceDistSuffix).value,
      targetDir = (LocalRootProject / sourceDistTargetDir).value.getAbsolutePath,
      incubating = (LocalRootProject / sourceDistIncubating).value,
      logger = streams.value.log
    )
  )

  override def requires = SbtPgp

  override lazy val buildSettings: Seq[Setting[_]] = sourceDistSettings

  override def trigger = allRequirements

}
