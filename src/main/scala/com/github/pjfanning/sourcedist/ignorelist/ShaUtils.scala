package com.github.pjfanning.sourcedist.ignorelist

import java.io.{File, FileInputStream, FileOutputStream, InputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.security.{DigestInputStream, MessageDigest}
import java.util.Base64
import scala.util.Using

object ShaUtils {
  def writeShaDigest(file: File, homeDir: String, keySize: Int): Unit = {
    val truncatedFileName = removeBasePath(file.getAbsolutePath, homeDir)
    val digester = MessageDigest.getInstance(s"SHA-$keySize")
    Using(new FileInputStream(file)) { fileStream =>
      Using(new DigestInputStream(fileStream, digester)) { digestStream =>
        digestStream.on(true)
        readStream(digestStream)
        val base64Digest = Base64.getEncoder.encodeToString(digester.digest())
        Using(new FileOutputStream(s"${file.getAbsolutePath}.sha$keySize")) { fos =>
          Using(new OutputStreamWriter(fos, StandardCharsets.UTF_8)) { writer =>
            writer.append(s"$truncatedFileName $base64Digest")
          }
        }
      }
    }
  }

  private def readStream(inputStream: InputStream): Unit = {
    val buffer = new Array[Byte](8192)
    var count = 0L
    var n = inputStream.read(buffer)
    while (n != -1) {
      count += n
      n = inputStream.read(buffer)
    }
  }

}
