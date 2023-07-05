package com.github.pjfanning.sourcedist

import ignorelist._

import java.io.File
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
  ): Unit = {
    val baseDir = new File(homeDir)
    val gitState = new GitState(baseDir)
    val files = if (gitState.isUnderGitControl) {
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
    val toZipFile = new File(targetDirFile, s"$suffixedName.zip")
    val toTgzFile = new File(targetDirFile, s"$suffixedName.tgz")

    if (toZipFile.exists()) {
      logger.info(s"Found previous zip artifact at ${toZipFile.getPath}, recreating")
      IO.delete(toZipFile)
    } else
      logger.info(s"Creating zip archive at ${toZipFile.getPath}")

    val rootDirName = FileUtils.getFileNameWithoutSuffix(toZipFile)
    IO.zip(files.map { file =>
             val truncatedFileName = removeBasePath(file.getAbsolutePath, homeDir)
             (file, s"$rootDirName/$truncatedFileName")
           },
           toZipFile,
           None
    )

    ShaUtils.writeShaDigest(toZipFile, 512)

    if (toTgzFile.exists()) {
      logger.info(s"Found previous tgz archive at ${toTgzFile.getPath}, recreating")
      IO.delete(toTgzFile)
    } else
      logger.info(s"Creating tar archive at ${toTgzFile.getPath}")
    TarUtils.tgzFiles(toTgzFile, files, homeDir)

    ShaUtils.writeShaDigest(toTgzFile, 512)
  }

  private def listLocalFiles(baseDir: File): Seq[File] = {
    val ignoreList = new IgnoreList(baseDir)
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

  private def listGitFiles(gitState: GitState, baseDir: File): Seq[File] = {
    gitState.lsTree().flatMap { path =>
      if (includeGitFile(path)) Some(new File(baseDir, path).getAbsoluteFile) else None
    }
  }

  private def includeGitFile(path: String): Boolean = {
    !path.startsWith(".git") &&
      !path.startsWith(".bsp") &&
      !path.startsWith(".asf") &&
      !path.contains(".DS_Store")
  }


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
