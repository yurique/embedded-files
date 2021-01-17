ThisBuild / organization := "com.yurique"
ThisBuild / homepage := Some(url("https://github.com/yurique/embedded-files"))
ThisBuild / licenses += ("MIT", url(
  "https://github.com/yurique/embedded-files/blob/main/LICENSE.md"
))
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
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / sonatypeProfileName := "yurique"
ThisBuild / publishArtifact in Test := false
ThisBuild / publishMavenStyle := true
ThisBuild / githubWorkflowPublishTargetBranches := Seq()
val scala212Version  = "2.12.12"
val scala213Version  = "2.13.4"
val scala3M3Version  = "3.0.0-M3"
val scala3RC1Version = "3.0.0-RC1-bin-20210113-8345078-NIGHTLY"

ThisBuild / scalaVersion := scala3RC1Version
ThisBuild / crossScalaVersions := Seq(
  scala3RC1Version,
  scala3M3Version,
  scala212Version,
  scala213Version
)

lazy val noPublish = Seq(
  publishLocal / skip := true,
  publish / skip := true,
  publishTo := Some(
    Resolver.file("Unused transient repository", file("target/unusedrepo"))
  )
)

lazy val addScalaReflect = Seq(
  libraryDependencies ++= (CrossVersion.partialVersion(
    scalaVersion.value
  ) match {
    case Some((2, _)) =>
      Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      )
    case _ => Seq()
  })
)

lazy val addJUnit = Seq(
  libraryDependencies ++= Seq(
    "junit"         % "junit"           % "4.11" % Test,
    ("com.novocode" % "junit-interface" % "0.11" % Test)
      .exclude("junit", "junit-dep")
  )
)

lazy val addScalacOptions = Seq(
  scalacOptions ++=
    (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq(
          "-language:experimental.macros"
        )
      case Some((3, _)) => Seq()
      case _            => Seq()
    })
)

lazy val addSharedScala3SourceDir = Seq(
  Compile / unmanagedSourceDirectories ++= (CrossVersion.partialVersion(
    scalaVersion.value
  ) match {
    case Some((2, _)) =>
      Seq(
        // TODO figure out a better way of doing this
        baseDirectory.value / "../src/main/scala-2"
      )
    case Some((3, _)) =>
      Seq(
        baseDirectory.value / "../src/main/scala-3"
      )
    case _ => Seq()
  }),
  Test / unmanagedSourceDirectories ++= (CrossVersion.partialVersion(
    scalaVersion.value
  ) match {
    case Some((2, _)) =>
      Seq(
        // TODO figure out a better way of doing this
        baseDirectory.value / "../src/test/scala-2"
      )
    case Some((3, _)) =>
      Seq(
        baseDirectory.value / "../src/test/scala-3"
      )
    case _ => Seq()
  })
)

lazy val addJUnitJS = Seq(
  // TODO
  test in Test := {}
)

lazy val `embedded-files-macro` =
  crossProject(JVMPlatform, JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("macro"))
    .jvmSettings(addJUnit)
    .jsSettings(addJUnitJS)
    .settings(addScalacOptions)
    .settings(addScalaReflect)
    .settings(addSharedScala3SourceDir)

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
