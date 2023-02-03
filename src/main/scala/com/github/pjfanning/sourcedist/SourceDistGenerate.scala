package com.github.pjfanning.sourcedist

import ignorelist._
import sbt.internal.util.ManagedLogger

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    val toZipFileName = s"$targetDir/$baseFileName.zip"
    val toTgzFileName = s"$targetDir/$baseFileName.tgz"

    logger.info(s"creating $toZipFileName")
    IO.zip(files.map { file =>
      (file, removeBasePath(file.getAbsolutePath, homeDir))
    }, new File(toZipFileName), None)
    logger.info(s"creating $toTgzFileName")
    TarUtils.tgzFiles(toTgzFileName, files, homeDir)
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
