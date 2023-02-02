package com.github.pjfanning.sourcedist.ignorelist

import org.apache.commons.io.IOUtils

import java.io.{File, FileInputStream, FileOutputStream}
import java.util.zip.{ZipEntry, ZipOutputStream}

object ZipUtils {
  def zipFiles(zipFileName: String, filesToInclude: Seq[File], homeDir: String): Unit = {
    val zipFile = new File(zipFileName)
    new File(zipFileName).mkdirs()
    if (zipFile.exists()) zipFile.delete()
    val zipFileStream = new FileOutputStream(zipFileName)
    try {
      val zos = new ZipOutputStream(zipFileStream)
      filesToInclude.sortBy(_.getAbsolutePath).foreach { f =>
        val truncatedFileName = removeBasePath(f.getAbsolutePath, homeDir)
        val zipEntry = new ZipEntry(truncatedFileName)
        zos.putNextEntry(zipEntry)
        val fis = new FileInputStream(f)
        try {
          IOUtils.copy(fis, zos)
        } finally {
          IOUtils.closeQuietly(fis)
        }
        zos.closeEntry()
      }
      zos.close()
    } finally {
      zipFileStream.close()
    }
  }
}
