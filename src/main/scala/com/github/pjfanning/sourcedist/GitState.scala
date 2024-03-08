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

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk

import java.io.File
import scala.collection.mutable
import scala.util.Using

class GitState(dir: File) {
  private val lsDir             = dir.getAbsoluteFile
  private lazy val gitDirOption = findGitDir(lsDir)

  def isUnderGitControl: Boolean = gitDirOption.nonEmpty

  def hasUncommittedChanges: Boolean = {
    val gitDir = getGitDir()
    Using.resource(createRepository(gitDir)) { repository =>
      val git    = new Git(repository)
      val status = git.status().call()
      status.hasUncommittedChanges
    }
  }

  def lsTree(): Seq[String] = {
    val gitDir = getGitDir()
    val prefix = if (gitDir.getParentFile == null) {
      ""
    } else {
      val start = removeStart(lsDir.getAbsolutePath, gitDir.getParentFile.getAbsolutePath)
      val sep   = File.separator
      if (start.isEmpty) start else s"${removeStart(start, sep)}$sep"
    }
    Using.resource(createRepository(gitDir)) { repository =>
      getRepositoryFileListing(repository, prefix)
    }
  }

  private def getGitDir(): File =
    gitDirOption.getOrElse(throw new IllegalStateException("Failed to find .git dir"))

  private def createRepository(gitDir: File) =
    new FileRepositoryBuilder().setGitDir(gitDir).readEnvironment.findGitDir.build

  // it is assumed that the `dir` file has an absolute path already
  // use dir.getAbsoluteFile if unsure
  private def findGitDir(dir: File): Option[File] = {
    val possibleGitDir = new File(dir, ".git")
    if (possibleGitDir.exists()) {
      Some(possibleGitDir)
    } else if (dir.getParentFile != null) {
      findGitDir(dir.getParentFile)
    } else {
      None
    }
  }

  private def getRepositoryFileListing(repository: Repository, prefix: String): Seq[String] = {
    val head = repository.findRef("HEAD")
    if (head == null) {
      throw new IllegalStateException("cannot find git HEAD")
    }
    val walk     = new RevWalk(repository)
    val commit   = walk.parseCommit(head.getObjectId)
    val tree     = commit.getTree
    val treeWalk = new TreeWalk(repository)
    treeWalk.addTree(tree)
    treeWalk.setRecursive(true)
    val buffer = mutable.Buffer[String]()
    while (treeWalk.next) {
      val path = treeWalk.getPathString
      if (path.startsWith(prefix)) buffer.append(path.substring(prefix.length))
    }
    buffer
  }

  private def removeStart(str: String, remove: String): String =
    if (str.isEmpty || remove.isEmpty) {
      str
    } else if (str.startsWith(remove)) {
      str.substring(remove.length)
    } else {
      str
    }
}
