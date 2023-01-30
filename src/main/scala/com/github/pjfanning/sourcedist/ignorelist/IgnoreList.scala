package com.github.pjfanning.sourcedist.ignorelist

import java.io.File
import scala.collection.mutable

object GitIgnore {
  val FILE_NAME = ".gitignore"
}

class IgnoreList(private val rootDir: File) {
  private val patternListCache = mutable.Map[File, PathPatternList]()
  private val patternDefaults = mutable.Buffer[PathPatternList]()
  addPatterns(rootDir)

  def addPatterns(dir: File): IgnoreList = addPatterns(dir, "")

  def addPatterns(dir: File, basePath: String): IgnoreList =
    addPatterns(getDirectoryPattern(dir, basePath))

  def addPatterns(patterns: PathPatternList): IgnoreList = {
    patternDefaults.append(patterns)
    this
  }

  def isExcluded(file: File): Boolean = {
    val filePath = ExcludeUtils.getRelativePath(rootDir, file)
    val pathBuilder = new java.lang.StringBuilder(filePath.length)
    val stack = patternDefaults.toIndexedSeq
    while (true) {
      var offset = filePath.indexOf('/', pathBuilder.length + 1)
      var isDirectory = true
      if (offset == -1) {
        offset = filePath.length
        isDirectory = file.isDirectory
      }
      pathBuilder.insert(pathBuilder.length, filePath, pathBuilder.length, offset)
      val currentPath = pathBuilder.toString
      stack.reverse.map { patterns =>
        patterns.findPattern(currentPath, isDirectory) match {
          case Some(pattern) => return pattern.isExclude
          case _ =>
        }
      }
      if (!isDirectory || pathBuilder.length >= filePath.length) return false
    }
    false
  }

  private def getDirectoryPattern(dir: File, dirPath: String): PathPatternList =
    getPatternList(new File(dir, GitIgnore.FILE_NAME), dirPath)

  private def getPatternList(file: File, basePath: String): PathPatternList = {
    patternListCache.get(file) match {
      case Some(list) => list
      case _ => {
        val list = ExcludeUtils.readExcludeFile(file, basePath)
        patternListCache.put(file, list)
        list
      }
    }
  }
}