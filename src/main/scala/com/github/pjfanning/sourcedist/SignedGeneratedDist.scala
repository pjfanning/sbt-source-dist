package com.github.pjfanning.sourcedist

import sbt.File

final case class SignedGeneratedDist(dist: File, checksum: File, detachedSignature: File)
