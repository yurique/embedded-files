version := "0.1"
scalaVersion := "2.13.4"

val root = project
  .in(file("."))
  .enablePlugins(EmbeddedFilesPlugin)
  .settings(
    embedTextGlobs := Seq("**/*.txt"),
    embedBinGlobs := Seq("**/*.bin"),
    embedDirectories ++= (Compile / unmanagedSourceDirectories).value,
    (Compile / sourceGenerators) += embedFiles
  )
