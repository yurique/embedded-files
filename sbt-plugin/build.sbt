ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.yurique"
ThisBuild / description := "An sbt plugin that generates scala objects embedding the contents of files."
ThisBuild / bintrayRepository := "sbt-plugins"
ThisBuild / bintrayOrganization in bintray := None
ThisBuild / bintrayPackageLabels := Seq("sbt", "plugin")
ThisBuild / bintrayVcsUrl := Some("""git@github.com:yurique/embedded-files.git""")
ThisBuild / licenses += ("MIT", url("https://github.com/yurique/embedded-files/LICENSE"))

name := "sbt-embedded-files"
sbtPlugin := true
publishMavenStyle := false
initialCommands in console := """import com.yurique.embedded.sbt._"""
enablePlugins(ScriptedPlugin)
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
