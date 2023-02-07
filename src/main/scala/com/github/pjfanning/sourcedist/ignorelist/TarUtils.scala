package com.github.pjfanning.sourcedist.ignorelist

import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveOutputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream

import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream, InputStream, OutputStream}
import scala.util.Using

object TarUtils {
  def tgzFiles(tarFile: File, filesToInclude: Seq[File], homeDir: String): Unit =
    Using(new FileOutputStream(tarFile)) { tarFileStream =>
      val buffOut = new BufferedOutputStream(tarFileStream)
      val gzOut   = new GzipCompressorOutputStream(buffOut)
      Using(new TarArchiveOutputStream(gzOut)) { tos =>
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
        filesToInclude.sortBy(_.getAbsolutePath).foreach { f =>
          val truncatedFileName = removeBasePath(f.getAbsolutePath, homeDir)
          val tarEntry          = new TarArchiveEntry(truncatedFileName)
          tarEntry.setSize(f.length())
          tos.putArchiveEntry(tarEntry)
          Using(new FileInputStream(f)) { fis =>
            copyLarge(fis, tos)
          }
          tos.closeArchiveEntry()
        }
      }
    }

  private def copyLarge(inputStream: InputStream, outputStream: OutputStream): Long = {
    val buffer = new Array[Byte](8192)
    var count  = 0L
    var n      = inputStream.read(buffer)
    while (n != -1) {
      outputStream.write(buffer, 0, n)
      count += n
      n = inputStream.read(buffer)
    }
    count
  }
}
