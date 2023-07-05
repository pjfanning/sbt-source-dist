package com.github.pjfanning.sourcedist

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import java.io.File

class GitStateSpec extends AnyFlatSpec with Matchers {
  "GitState" must "support lsTree" in {
    val gitState = new GitState(new File("."))
    gitState.isUnderGitControl mustBe true
    val seq = gitState.lsTree()
    seq must contain("README.md")
    seq must contain(".github/workflows/ci.yml")
    seq.filter(_.startsWith("target")) mustBe empty
  }
  it must "support lsTree on subdir" in {
    val gitState = new GitState(new File(".github"))
    gitState.isUnderGitControl mustBe true
    val seq = gitState.lsTree()
    seq must contain("workflows/ci.yml")
    seq must not contain "README.md"
  }
}
