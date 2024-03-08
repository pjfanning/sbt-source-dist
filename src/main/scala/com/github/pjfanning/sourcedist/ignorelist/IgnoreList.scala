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

package com.github.pjfanning.sourcedist.ignorelist

import java.io.File
import scala.collection.mutable

class IgnoreList(private val rootDir: File) {
  private val patternListCache = mutable.Map[File, PathPatternList]()
  private val patternDefaults  = mutable.Buffer[PathPatternList]()
  addPatterns(rootDir)

  def addPatterns(dir: File): Unit = addPatterns(dir, "")

  def addPatterns(dir: File, basePath: String): Unit =
    getDirectoryPattern(dir, basePath).map(addPatterns)

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

  private def getDirectoryPattern(dir: File, dirPath: String): Option[PathPatternList] =
    getPatternList(new File(dir, GitIgnore.FILE_NAME), dirPath)

  private def getPatternList(file: File, basePath: String): Option[PathPatternList] =
    patternListCache.get(file) match {
      case Some(list) => Some(list)
      case _ =>
        if (file.exists()) {
          val list = ExcludeUtils.readExcludeFile(file, basePath)
          patternListCache.put(file, list)
          Some(list)
        } else {
          None
        }
    }
}
