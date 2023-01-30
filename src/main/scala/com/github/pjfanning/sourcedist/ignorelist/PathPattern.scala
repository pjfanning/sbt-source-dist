/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Jonas Fonseca <fonseca@diku.dk>
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file.
 *
 * This particular file is subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.github.pjfanning.sourcedist.ignorelist

object PathPattern {
  def create(pattern: String): PathPattern = {
    if (hasNoWildcards(pattern, 0, pattern.length)) new PathPattern.NoWildcardPathPattern(pattern)
    else new PathPattern.WildcardPathPattern(pattern)
  }

  private def isWildcard(c: Char) = c == '*' || c == '[' || c == '?' || c == '\\'

  private def hasNoWildcards(pattern: String, from: Int, to: Int): Boolean = {
    (from until to).collectFirst { i =>
      !isWildcard(pattern.charAt(i))
    }.isEmpty
  }

  private class NoWildcardPathPattern(pattern: String) extends PathPattern(pattern) {
    override protected def matchesFileName(path: String): Boolean = {
      if (path.length > pattern.length && path.charAt(path.length - pattern.length - 1) != '/') {
        false
      } else {
        path.endsWith(pattern)
      }
    }

    override protected def matchesPathName(path: String, basePath: String): Boolean = {
      val baseLength = if (basePath.length > 0) basePath.length + 1
      else 0
      path.length - baseLength == pattern.length && path.startsWith(pattern, baseLength)
    }
  }

  private class WildcardPathPattern(patternString: String) extends PathPattern(patternString) {

    private val (regionFrom, regionLength) = if (matchFileName) {
      val (from, to) = if (pattern.startsWith("*")) {
        (1, pattern.length)
      } else if (pattern.endsWith("*")) {
        (0, pattern.length - 1)
      } else {
        (0, 0)
      }
      if (hasNoWildcards(pattern, from, to)) {
        (from, to - from)
      } else {
        (0, 0)
      }
    } else {
      (0, 0)
    }

    override protected def matchesFileName(path: String): Boolean = {
      val from = path.lastIndexOf('/') + 1
      if (from >= path.length) {
        false
      } else if (regionLength > 0) {
        val offset = if (regionFrom > 0) path.length - regionLength else from
        offset >= from && path.regionMatches(offset, pattern, regionFrom, regionLength)
      } else {
        FnMatch.fnmatch(pattern, path, from)
      }
    }

    override protected def matchesPathName(path: String, basePath: String): Boolean = {
      val baseLength = if (basePath.length > 0) basePath.length + 1 else 0
      FnMatch.fnmatch(pattern, path, baseLength, FnMatch.Flag.PATHNAME)
    }
  }
}

abstract class PathPattern(inputPattern: String) {

  protected val (pattern, exclude, matchFileName, matchDir) = {
    val builder = new StringBuilder(inputPattern)
    val exclude = inputPattern.charAt(0) != '!'
    if (!exclude) builder.deleteCharAt(0)
    val matchDir = inputPattern.endsWith("/")
    if (matchDir) builder.deleteCharAt(builder.length - 1)
    val matchFileName = if (builder.charAt(0) == '/') {
      builder.deleteCharAt(0)
      false
    } else {
      builder.indexOf("/") == -1
    }
    (builder.toString, exclude, matchFileName, matchDir)
  }

  def isExclude: Boolean = exclude

  def matches(path: String, isDirectory: Boolean, basePath: String): Boolean = {
    if (matchDir && !isDirectory) return false
    if (matchFileName && matchesFileName(path)) return true
    if (basePath.length > 0 && !path.startsWith(basePath)) return false
    matchesPathName(path, basePath)
  }

  protected def matchesFileName(path: String): Boolean

  protected def matchesPathName(path: String, basePath: String): Boolean

  override def toString: String = {
    val builder = new StringBuilder
    builder.append(if (exclude) "EX" else "IN").append("CLUDE(")
    builder.append(pattern)
    if (!matchFileName) builder.append(", path")
    if (matchDir) builder.append(", dirs")
    builder.append(")").toString
  }
}
