package com.github.pjfanning.sourcedist

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

  def lsTree(): Seq[String] = {
    val gitDir = gitDirOption.getOrElse(throw new IllegalStateException("Failed to find .git dir"))
    val prefix = if (gitDir.getParentFile == null) {
      ""
    } else {
      val start = removeStart(lsDir.getAbsolutePath, gitDir.getParentFile.getAbsolutePath)
      val sep   = File.separator
      if (start.isEmpty) start else s"${removeStart(start, sep)}$sep"
    }
    Using.resource(new FileRepositoryBuilder().setGitDir(gitDir).readEnvironment.findGitDir.build) { repository =>
      getRepositoryFileListing(repository, prefix)
    }
  }

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
