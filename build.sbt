inThisBuild(
  List(
    organization := "com.yurique",
    homepage := Some(url("https://github.com/yurique/embedded-files")),
    licenses += ("MIT", url("https://github.com/yurique/embedded-files/LICENSE")),
    developers := List(Developer("yurique", "Iurii Malchenko", "i@yurique.com", url("https://github.com/yurique"))),
    description := "An sbt plugin that generates Scala objects embedding the contents of files.",
    scmInfo := Some(ScmInfo(url("https://github.com/yurique/embedded-files"), "scm:git@github.com/yurique/embedded-files.git")),
    githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("scripted"))),
    githubWorkflowTargetTags ++= Seq("v*"),
    githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
    githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release"))),
    githubWorkflowEnv ~= (_ ++ Map(
      "PGP_PASSPHRASE" -> s"$${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> s"$${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> s"$${{ secrets.SONATYPE_USERNAME }}"
    )),
    scalaVersion := "2.13.8"
  )
)

name := "sbt-embedded-files"
sbtPlugin := true
publishMavenStyle := true
console / initialCommands := """import com.yurique.embedded.sbt._"""
enablePlugins(ScriptedPlugin)
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

// test := scripted.value
