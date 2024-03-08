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
