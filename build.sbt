inThisBuild(
  List(
    organization := "com.yurique",
    description := "An sbt plugin that generates scala objects embedding the contents of files.",
    licenses += ("MIT", url("https://github.com/yurique/embedded-files/LICENSE")),
    githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("scripted"))),
    githubWorkflowTargetTags ++= Seq("v*"),
    githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
    githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release"))),
    githubWorkflowEnv ~= (_ ++ Map(
      "PGP_PASSPHRASE" -> s"$${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> s"$${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> s"$${{ secrets.SONATYPE_USERNAME }}"
    ))
  )
)

name := "sbt-embedded-files"
scalaVersion := "2.12.14"
sbtPlugin := true
publishMavenStyle := true
console / initialCommands := """import com.yurique.embedded.sbt._"""
enablePlugins(ScriptedPlugin)
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

// test := scripted.value
