libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("org.typelevel"    % "sbt-typelevel-sonatype-ci-release" % "0.5.0-RC5")
addSbtPlugin("com.github.sbt"   % "sbt-ci-release"                    % "1.5.12")
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"                      % "2.5.0")
