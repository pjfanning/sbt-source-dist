package com.github.pjfanning.sourcedist.ignorelist

import java.io.File
import scala.collection.mutable

class IgnoreList(private val rootDir: File) {
  private val patternListCache = mutable.Map[File, PathPatternList]()
  private val patternDefaults  = mutable.Buffer[PathPatternList]()
  addPatterns(rootDir)

  def addPatterns(dir: File): IgnoreList = addPatterns(dir, "")

  def addPatterns(dir: File, basePath: String): IgnoreList =
    addPatterns(getDirectoryPattern(dir, basePath))

  def addPatterns(patterns: PathPatternList): IgnoreList = {
    patternDefaults.append(patterns)
    this
  }

  def isExcluded(file: File): Boolean = {
    val filePath    = ExcludeUtils.getRelativePath(rootDir, file)
    val pathBuilder = new java.lang.StringBuilder(filePath.length)
    val stack       = patternDefaults.toIndexedSeq
    var loop        = true
    var result      = false
    while (loop) {
      val pathOffset = filePath.indexOf('/', pathBuilder.length + 1)
      val (offset, isDirectory) = if (pathOffset == -1) {
        (filePath.length, file.isDirectory)
      } else {
        (pathOffset, true)
      }
      pathBuilder.insert(pathBuilder.length, filePath, pathBuilder.length, offset)
      val currentPath = pathBuilder.toString
      val iter        = stack.reverseIterator
      while (loop && iter.hasNext) {
        val patterns = iter.next()
        patterns.findPattern(currentPath, isDirectory) match {
          case Some(pattern) =>
            result = pattern.isExclude
            loop = false
          case _ =>
        }
      }
      if (loop && (!isDirectory || pathBuilder.length >= filePath.length)) {
        result = false
        loop = false
      }
    }
    result
  }

  private def getDirectoryPattern(dir: File, dirPath: String): PathPatternList =
    getPatternList(new File(dir, GitIgnore.FILE_NAME), dirPath)

  private def getPatternList(file: File, basePath: String): PathPatternList =
    patternListCache.get(file) match {
      case Some(list) => list
      case _ =>
        val list = ExcludeUtils.readExcludeFile(file, basePath)
        patternListCache.put(file, list)
        list
    }
}
