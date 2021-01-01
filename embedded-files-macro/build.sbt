ThisBuild / organization := "com.yurique"
ThisBuild / homepage := Some(url("https://github.com/yurique/embedded-files"))
ThisBuild / licenses += ("MIT", url("https://github.com/yurique/embedded-files/blob/main/LICENSE.md"))
ThisBuild / developers := List(
  Developer(
    id = "yurique",
    name = "Iurii Malchenko",
    email = "i@yurique.com",
    url = url("https://github.com/yurique")
  )
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/yurique/embedded-files"),
    "scm:git@github.com/yurique/embedded-files.git"
  )
)
ThisBuild / releasePublishArtifactsAction := PgpKeys.publishSigned.value
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / sonatypeProfileName := "yurique"
ThisBuild / publishArtifact in Test := false
ThisBuild / publishMavenStyle := true
ThisBuild / releaseCrossBuild := true

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / crossScalaVersions := Seq("2.12.12", "2.13.4")

lazy val noPublish = Seq(
  publishLocal / skip := true,
  publish / skip := true,
  publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))
)

lazy val `embedded-files-macro` = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("macro"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )

lazy val root = project
  .in(file("."))
  .settings(
    name := "embedded-files"
  )
  .settings(noPublish)
  .aggregate(
    `embedded-files-macro`.jvm,
    `embedded-files-macro`.js
  )
