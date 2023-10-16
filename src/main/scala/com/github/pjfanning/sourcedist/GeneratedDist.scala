package com.github.pjfanning.sourcedist

import sbt.File

final case class GeneratedDist(dist: File, checksum: File) {
  def toSignedGeneratedDist(detachedSignature: File): SignedGeneratedDist =
    SignedGeneratedDist(dist, checksum, detachedSignature)
}
