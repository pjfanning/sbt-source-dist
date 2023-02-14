package com.github.pjfanning.sourcedist

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
class UtilSpec extends AnyFlatSpec with Matchers {
  "IgnoreList Utils" must "support removeBasePath" in {
    val fname = "/home/user123/file.txt"
    removeBasePath(fname, "") mustEqual fname.substring(1)
    removeBasePath(fname, "/xyz") mustEqual fname.substring(1)
    removeBasePath(fname, "home") mustEqual fname.substring(1)
    removeBasePath(fname, "/home") mustEqual "user123/file.txt"
    removeBasePath(fname, "/home/") mustEqual "user123/file.txt"
    removeBasePath(fname, "/home/user123") mustEqual "file.txt"
  }
}
