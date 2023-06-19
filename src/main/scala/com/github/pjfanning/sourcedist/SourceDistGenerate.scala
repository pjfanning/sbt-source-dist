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
                                              logger: ManagedLogger
  ): Unit = {
    val baseDir = new File(homeDir)

    val ignoreList           = new IgnoreList(baseDir)
    val customIgnorePatterns = new PathPatternList("")
    customIgnorePatterns.add("target/")
    customIgnorePatterns.add(".git/")
    customIgnorePatterns.add(".github/")
    customIgnorePatterns.add(".git*")
    customIgnorePatterns.add(".asf.yaml")
    ignoreList.addPatterns(customIgnorePatterns)
    val files = getIncludedFiles(baseDir, ignoreList)

    val baseFileName  = s"$prefix-src-$version"
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

    ShaUtils.writeShaDigest(toZipFile, 256)
    ShaUtils.writeShaDigest(toZipFile, 512)

    if (toTgzFile.exists()) {
      logger.info(s"Found previous tgz archive at ${toTgzFile.getPath}, recreating")
      IO.delete(toTgzFile)
    } else
      logger.info(s"Creating tar archive at ${toTgzFile.getPath}")
    TarUtils.tgzFiles(toTgzFile, files, homeDir)

    ShaUtils.writeShaDigest(toTgzFile, 256)
    ShaUtils.writeShaDigest(toTgzFile, 512)
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
