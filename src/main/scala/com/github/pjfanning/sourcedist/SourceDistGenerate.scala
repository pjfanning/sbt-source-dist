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

import ignorelist._

import java.io.File
import java.time.ZoneOffset
import sbt.internal.util.ManagedLogger
import sbt.io.IO


private[sourcedist] object SourceDistGenerate {
  private[sourcedist] def generateSourceDists(homeDir: String,
                                              prefix: String,
                                              version: String,
                                              targetDir: String,
                                              suffix: String,
                                              incubating: Boolean,
                                              logger: ManagedLogger
  ): GeneratedDist = {
    val baseDir  = new File(homeDir)
    val gitState = new GitState(baseDir)
    val files = if (gitState.isUnderGitControl) {
      if (gitState.hasUncommittedChanges) {
        throw new IllegalStateException(
          "Local Git Repository has uncommitted changes. Please revert or commit these changes before trying again."
        )
      }
      listGitFiles(gitState, baseDir)
    } else {
      listLocalFiles(baseDir)
    }

    val versionString = if (incubating) s"$version-incubating" else version
    val baseFileName  = s"$prefix-$versionString-src"
    val suffixedName  = if (suffix.nonEmpty) s"$baseFileName-$suffix" else baseFileName
    val targetDirFile = new File(targetDir)
    if (!targetDirFile.exists())
      IO.createDirectory(targetDirFile)

    val toTgzFile = new File(targetDirFile, s"$suffixedName.tgz")
    if (toTgzFile.exists()) {
      logger.info(s"Found previous tgz archive at ${toTgzFile.getPath}, recreating")
      IO.delete(toTgzFile)
    } else {
      logger.info(s"Creating tar archive at ${toTgzFile.getPath}")
      logger.info(s"Using SOURCE_DATE_EPOCH=${TarUtils.fileTimeLong} ${TarUtils.fileTime.toInstant.atOffset(ZoneOffset.UTC)}")
    }
    TarUtils.tgzFiles(toTgzFile, files, homeDir)
    GeneratedDist(toTgzFile, ShaUtils.writeShaDigest(toTgzFile, 512))
  }

  private def listLocalFiles(baseDir: File): Seq[File] = {
    val ignoreList           = new IgnoreList(baseDir)
    val customIgnorePatterns = new PathPatternList("")
    customIgnorePatterns.add("target/")
    customIgnorePatterns.add(".git/")
    customIgnorePatterns.add(".github/")
    customIgnorePatterns.add(".git*")
    customIgnorePatterns.add(".bsp/")
    customIgnorePatterns.add(".idea/")
    customIgnorePatterns.add(".vscode/")
    customIgnorePatterns.add(".DS_Store")
    customIgnorePatterns.add(".asf.yaml")
    ignoreList.addPatterns(customIgnorePatterns)
    getIncludedFiles(baseDir, ignoreList)
  }

  private def listGitFiles(gitState: GitState, baseDir: File): Seq[File] =
    gitState.lsTree().flatMap { path =>
      if (includeGitFile(path)) Some(new File(baseDir, path).getAbsoluteFile) else None
    }

  private def includeGitFile(path: String): Boolean =
    !path.startsWith(".git") &&
      !path.startsWith(".bsp") &&
      !path.startsWith(".asf") &&
      !path.contains(".DS_Store")

  private def getIncludedFiles(dir: File, ignoreList: IgnoreList): Seq[File] = {
    val files = dir.listFiles().filterNot(ignoreList.isExcluded).toSeq
    files.flatMap { file =>
      if (file.isDirectory) {
        getIncludedFiles(file, ignoreList)
      } else {
        Seq(file)
      }
    }
  }
}
