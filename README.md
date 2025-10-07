# sbt-source-dist

An sbt plugin to produce source distributions for
[Apache Pekko](https://github.com/apache/pekko).

* Outputs to target/dist directory of the root project
* Since v0.1.13, we support [SOURCE_DATE_EPOCH](https://reproducible-builds.org/docs/source-date-epoch/) where the file times for the files inside the generated tar archive are set based on the environment variable (SOURCE_DATE_EPOCH). This is for reproducible build purposes. The value is the number of seconds since the start of 1970-01-01. Defaults to zero.

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

sbt-source-dist also supports signing the final generated source distribution
using [sbt-pgp](https://github.com/sbt/sbt-pgp). Signing the source
distribution in this manner has the added advantage of ensuring that the same
signing key that is used to sign binary maven artifacts is also used for the
source dist.

You can both generate the source distribution and then sign it using

```
sbt signedSourceDistGenerate
```

`signedSourceDistGenerate` will re-use the same settings provided by sbt-pgp
so if you need to configure it follow the instructions at https://github.com/sbt/sbt-pgp#usage.
