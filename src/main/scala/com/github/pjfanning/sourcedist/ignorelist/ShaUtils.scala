package com.github.pjfanning.sourcedist.ignorelist

import java.io.{File, FileInputStream, FileOutputStream, InputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.security.{DigestInputStream, MessageDigest}
import scala.util.Using

object ShaUtils {
  def writeShaDigest(file: File, keySize: Int): Unit = {
    val digester = MessageDigest.getInstance(s"SHA-$keySize")
    Using(new FileInputStream(file)) { fileStream =>
      Using(new DigestInputStream(fileStream, digester)) { digestStream =>
        digestStream.on(true)
        readStream(digestStream)
        val hexDigest = convertBytesToHexadecimal(digester.digest())
        Using(new FileOutputStream(s"${file.getAbsolutePath}.sha$keySize")) { fos =>
          Using(new OutputStreamWriter(fos, StandardCharsets.UTF_8)) { writer =>
            writer.append(s"$hexDigest  ${file.getName}")
          }
        }
      }
    }
  }

  private def readStream(inputStream: InputStream): Unit = {
    val buffer = new Array[Byte](8192)
    var count  = 0L
    var n      = inputStream.read(buffer)
    while (n != -1) {
      count += n
      n = inputStream.read(buffer)
    }
  }

  private def convertBytesToHexadecimal(byteArray: Array[Byte]): String = {
    val hexBuilder = new StringBuilder()
    byteArray.foreach { b =>
      val decimal: Int = b & 0xff
      val hex: String  = Integer.toHexString(decimal)
      if (hex.length % 2 == 1) { // if half hex, pad with zero, e.g \t
        hexBuilder.append('0')
      }
      hexBuilder.append(hex)
    }
    hexBuilder.toString()
  }
}
