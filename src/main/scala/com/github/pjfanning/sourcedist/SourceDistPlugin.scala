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

package com.github.pjfanning.sourcedist

import sbt._
import sbt.Keys._
import com.jsuereth.sbtpgp.PgpKeys.{pgpSigner, pgpSigningKey}
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
        pgpSigningKey.value match {
          case Some(customKey) => s.log.info(s"Signing source distribution using custom key: $customKey")
          case None            => s.log.info(s"Signing source distribution using default gpg key")
        }
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

  override lazy val requires = SbtPgp

  override lazy val buildSettings: Seq[Setting[_]] = sourceDistSettings

  override lazy val trigger = allRequirements

}
