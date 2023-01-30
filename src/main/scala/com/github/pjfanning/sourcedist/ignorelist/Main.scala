package com.github.pjfanning.sourcedist.ignorelist

import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveOutputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.io.IOUtils

import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.{ZipEntry, ZipOutputStream}

object Main extends App {
  val homeDir = "/Users/pj.fanning/code/incubator-pekko"
  val baseDir = new File(homeDir)

  val ignoreList = new IgnoreList(baseDir)
  val customIgnorePatterns = new PathPatternList("")
  customIgnorePatterns.add("target/")
  customIgnorePatterns.add(".git/")
  customIgnorePatterns.add(".github/")
  customIgnorePatterns.add(".git*")
  customIgnorePatterns.add(".bsp")
  customIgnorePatterns.add(".asf.yaml")
  ignoreList.addPatterns(customIgnorePatterns)
  val files = getIncludedFiles(baseDir, ignoreList)
  //files.sortBy(_.getAbsolutePath).foreach(f => println(removeBasePath(f.getAbsolutePath, homeDir)))

  val dateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE
  val dateString = LocalDate.now().format(dateTimeFormatter)
  // TODO remove hardcoded version - needs to become a param derived from git tag (or a property)
  val version = "0.0.0"
  val toFileDir = s"$homeDir/target/dist/"
  val baseFileName = s"incubator-pekko-src-$version-$dateString"
  val toZipFileName = s"$toFileDir/$baseFileName.zip"
  val toTgzFileName = s"$toFileDir/$baseFileName.tgz"
  val toZipFile = new File(toZipFileName)
  val toTgzFile = new File(toTgzFileName)
  new File(toFileDir).mkdirs()
  if (toZipFile.exists()) toZipFile.delete()
  val toZipFileStream = new FileOutputStream(toZipFile)
  if (toTgzFile.exists()) toTgzFile.delete()
  val toTgzFileStream = new FileOutputStream(toTgzFile)
  try {
    val zos = new ZipOutputStream(toZipFileStream)
    val buffOut = new BufferedOutputStream(toTgzFileStream)
    val gzOut = new GzipCompressorOutputStream(buffOut);
    val tos = new TarArchiveOutputStream(gzOut)
    tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)

    files.sortBy(_.getAbsolutePath).foreach { f =>
      val truncatedFileName = removeBasePath(f.getAbsolutePath, homeDir)
      println(truncatedFileName)
      val zipEntry = new ZipEntry(truncatedFileName)
      zos.putNextEntry(zipEntry)
      val fis = new FileInputStream(f)
      try {
        IOUtils.copy(fis, zos)
      } finally {
        IOUtils.closeQuietly(fis)
      }
      zos.closeEntry()

      val tarEntry = new TarArchiveEntry(truncatedFileName)
      tarEntry.setSize(f.length())
      tos.putArchiveEntry(tarEntry)
      val fis2 = new FileInputStream(f)
      try {
        IOUtils.copy(fis2, tos)
      } finally {
        IOUtils.closeQuietly(fis2)
      }
      tos.closeArchiveEntry()
    }
    zos.close()
    tos.close()
  } finally {
    toZipFileStream.close()
    toTgzFileStream.close()
  }

  private def removeBasePath(fileName: String, basePath: String): String = {
    val truncated = if (fileName.startsWith(basePath)) {
      fileName.substring(basePath.length)
    } else {
      fileName
    }
    if (truncated.startsWith("/")) {
      truncated.substring(1)
    } else {
      truncated
    }
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
