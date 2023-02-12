# sbt-source-dist

An attempt to prototype the code to produce source distributions for
[Apache Pekko](https://github.com/apache/incubator-pekko).

* Some code comes from https://github.com/neva-dev/gitignore-file-filter
* Some of the code was converted from the original Java to Scala - partially via IntelliJ autoconversion - some of that
  code needs work - to better follow Scala norms
* Uses the .gitignore and some extra custom patterns to exclude files from the generated zip and tgz files
    * support for nested .gitignore files has not been retained (it can be added back if it is actually useful)
* Outputs to target/dist directory of the root project

## sbt plugin

sbt-source-dist is designed as an [AutoPlugin](https://www.scala-sbt.org/1.x/docs/Plugins.html) that immediately
triggers onto the root project. If necessary one can override one of
projects [keys](/src/main/scala/com/github/pjfanning/sourcedist/SourceDistKeys.scala), i.e. if the name of your project
doesn't your Apache projects name you can do

```sbt
sourceDistName := "My Apache Project"
```

If you have an explicit root project then you can do the following

```sbt
val root = Project(id = "root", base = file(".")).settings(
  sourceDistName := "My Apache Project"
)
```

You can then generate the source distribution using

```
sbt sourceDistGenerate
```
