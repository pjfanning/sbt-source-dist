package com.github.pjfanning.sourcedist

import java.io.{File, FileInputStream, FileOutputStream, InputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.security.{DigestInputStream, MessageDigest}
import scala.util.{Failure, Success, Using}

object ShaUtils {
  def writeShaDigest(file: File, keySize: Int): File = {
    val digester = MessageDigest.getInstance(s"SHA-$keySize")
    Using(new FileInputStream(file)) { fileStream =>
      Using(new DigestInputStream(fileStream, digester)) { digestStream =>
        digestStream.on(true)
        readStream(digestStream)
        val hexDigest      = convertBytesToHexadecimal(digester.digest())
        val outputFileName = new File(s"${file.getAbsolutePath}.sha$keySize")
        Using(new FileOutputStream(outputFileName)) { fos =>
          Using(new OutputStreamWriter(fos, StandardCharsets.UTF_8)) { writer =>
            writer.append(s"$hexDigest  ${file.getName}")
          }
        }
        outputFileName
      }
    }.flatten match {
      case Failure(exception) => throw exception
      case Success(value)     => value
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

  private[sourcedist] def convertBytesToHexadecimal(byteArray: Array[Byte]): String = {
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
