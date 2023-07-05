package com.github.pjfanning.sourcedist

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import java.io.File

class GitUtilsSpec extends AnyFlatSpec with Matchers {
  "GitUtils" must "support lsTree" in {
    val seq = GitUtils.lsTree(new File("."))
    seq must contain("README.md")
    seq must contain(".github/workflows/ci.yml")
  }
  it must "support lsTree on subdir" in {
    val seq = GitUtils.lsTree(new File(".github"))
    seq must contain("workflows/ci.yml")
    seq must not contain "README.md"
  }
}
