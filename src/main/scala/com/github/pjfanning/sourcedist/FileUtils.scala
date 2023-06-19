package com.github.pjfanning.sourcedist

import java.io.File

object FileUtils {
  def getFileNameWithoutSuffix(file: File): String = {
    val fileName = file.getName
    val idx      = fileName.lastIndexOf('.')
    if (idx == -1) {
      fileName
    } else {
      fileName.substring(0, idx)
    }
  }
}
