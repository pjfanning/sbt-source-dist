package com.github.pjfanning.sourcedist.ignorelist

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import sbt.io.IO

object Main extends App {
  val homeDirectory = "/Users/pj.fanning/code/incubator-pekko"
  val namePrefix = "incubator-pekko"
  // TODO remove hardcoded version - needs to become a param derived from git tag (or a property)
  val versionString = "0.0.0"

  generateSourceDists(homeDirectory, namePrefix, versionString)

  private def generateSourceDists(homeDir: String, prefix: String, version: String): Unit = {
    val baseDir = new File(homeDir)

    val ignoreList = new IgnoreList(baseDir)
    val customIgnorePatterns = new PathPatternList("")
    customIgnorePatterns.add("target/")
    customIgnorePatterns.add(".git/")
    customIgnorePatterns.add(".github/")
    customIgnorePatterns.add(".git*")
    customIgnorePatterns.add(".asf.yaml")
    ignoreList.addPatterns(customIgnorePatterns)
    val files = getIncludedFiles(baseDir, ignoreList).sortBy(_.getAbsolutePath)
    //files.foreach(f => println(removeBasePath(f.getAbsolutePath, homeDir)))

    val dateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE
    val dateString = LocalDate.now().format(dateTimeFormatter)
    val toFileDir = s"$homeDir/target/dist/"
    val baseFileName = s"$prefix-src-$version-$dateString"
    val toZipFileName = s"$toFileDir/$baseFileName.zip"
    val toTgzFileName = s"$toFileDir/$baseFileName.tgz"
    println(toZipFileName)
    IO.zip(files.map { file =>
      (file, removeBasePath(file.getAbsolutePath, homeDir))
    }, new File(toZipFileName), None)
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
