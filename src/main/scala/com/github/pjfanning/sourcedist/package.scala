package com.github.pjfanning

package object sourcedist {
  private[sourcedist] def removeBasePath(fileName: String, basePath: String): String = {
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
}
