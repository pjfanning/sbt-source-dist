package com.github.pjfanning.sourcedist.ignorelist

import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveOutputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.io.IOUtils

import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream}

object TarUtils {
  def tgzFiles(tarFileName: String, filesToInclude: Seq[File], homeDir: String): Unit = {
    val tarFile = new File(tarFileName)
    new File(tarFileName).mkdirs()
    if (tarFile.exists()) tarFile.delete()
    val tarFileStream = new FileOutputStream(tarFileName)
    try {
      val buffOut = new BufferedOutputStream(tarFileStream)
      val gzOut = new GzipCompressorOutputStream(buffOut);
      val tos = new TarArchiveOutputStream(gzOut)
      tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
      filesToInclude.sortBy(_.getAbsolutePath).foreach { f =>
        val truncatedFileName = removeBasePath(f.getAbsolutePath, homeDir)
        val tarEntry = new TarArchiveEntry(truncatedFileName)
        tarEntry.setSize(f.length())
        tos.putArchiveEntry(tarEntry)
        val fis = new FileInputStream(f)
        try {
          IOUtils.copy(fis, tos)
        } finally {
          IOUtils.closeQuietly(fis)
        }
        tos.closeArchiveEntry()
      }
      tos.close()
    } finally {
      tarFileStream.close()
    }
  }
}
