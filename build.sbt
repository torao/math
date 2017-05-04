name := "math"

organization := "at.hazm"

version := "1.0.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.8.+" % "test"
)

publishTo := Some(Resolver.file("file",  new File("repo")))

// disable using the Scala version in output paths and artifacts
crossPaths := false

pomExtra :=
  <licenses>
    <license>
      <name>MIT</name>
      <url>https://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>