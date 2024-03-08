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

import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveOutputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream

import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream, InputStream, OutputStream}
import scala.util.Using

object TarUtils {
  def tgzFiles(tarFile: File, filesToInclude: Seq[File], homeDir: String): Unit = {
    val rootDirName = FileUtils.getFileNameWithoutSuffix(tarFile)
    Using(new FileOutputStream(tarFile)) { tarFileStream =>
      val buffOut = new BufferedOutputStream(tarFileStream)
      val gzOut   = new GzipCompressorOutputStream(buffOut)
      Using(new TarArchiveOutputStream(gzOut)) { tos =>
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
        filesToInclude.sortBy(_.getAbsolutePath).foreach { f =>
          val truncatedFileName = removeBasePath(f.getAbsolutePath, homeDir)
          val tarEntry          = new TarArchiveEntry(s"$rootDirName/$truncatedFileName")
          tarEntry.setSize(f.length())
          tos.putArchiveEntry(tarEntry)
          Using(new FileInputStream(f)) { fis =>
            copyLarge(fis, tos)
          }
          tos.closeArchiveEntry()
        }
      }
    }
  }

  private[sourcedist] def copyLarge(inputStream: InputStream, outputStream: OutputStream): Long = {
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
