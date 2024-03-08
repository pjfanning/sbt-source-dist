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
