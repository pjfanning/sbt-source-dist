package com.github.pjfanning.sourcedist

import ignorelist._

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import sbt.internal.util.ManagedLogger
import sbt.io.IO

private[sourcedist] object SourceDistGenerate {
  private[sourcedist] def generateSourceDists(homeDir: String,
                                              prefix: String,
                                              version: String,
                                              targetDir: String,
                                              logger: ManagedLogger): Unit = {
    val baseDir = new File(homeDir)

    val ignoreList = new IgnoreList(baseDir)
    val customIgnorePatterns = new PathPatternList("")
    customIgnorePatterns.add("target/")
    customIgnorePatterns.add(".git/")
    customIgnorePatterns.add(".github/")
    customIgnorePatterns.add(".git*")
    customIgnorePatterns.add(".asf.yaml")
    ignoreList.addPatterns(customIgnorePatterns)
    val files = getIncludedFiles(baseDir, ignoreList)

    val dateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE
    val dateString = LocalDate.now().format(dateTimeFormatter)
    val baseFileName = s"$prefix-src-$version-$dateString"
    IO.createDirectory(new File(targetDir))
    val toZipFile = new File(s"$targetDir/$baseFileName.zip")
    val toTgzFile = new File(s"$targetDir/$baseFileName.tgz")

    if (toZipFile.exists()) {
      logger.info(s"Found previous zip artifact at ${toZipFile.getPath}, recreating")
      IO.delete(toZipFile)
    } else
      logger.info(s"Creating zip archive at ${toZipFile.getPath}")

    IO.zip(files.map { file =>
      (file, removeBasePath(file.getAbsolutePath, homeDir))
    }, toZipFile, None)

    if (toTgzFile.exists()) {
      logger.info(s"Found previous tgz archive at ${toTgzFile.getPath}, recreating")
      IO.delete(toTgzFile)
    } else
      logger.info(s"Creating tar archive at ${toTgzFile.getPath}")
    TarUtils.tgzFiles(toTgzFile, files, homeDir)
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
