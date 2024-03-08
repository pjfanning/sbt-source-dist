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
  "FileUtils.getFileNameWithoutSuffix" must "remove extension from filename" in {
    val fname = "/home/user123/file.txt"
    FileUtils.getFileNameWithoutSuffix(new File(fname)) mustEqual "file"
  }
}
