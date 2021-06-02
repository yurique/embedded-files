version := "0.1"
scalaVersion := "2.13.6"

val root = project
  .in(file("."))
  .enablePlugins(EmbeddedFilesPlugin)
  .settings(
    embedTextGlobs := Seq("**/*.txt"),
    embedBinGlobs := Seq("**/*.bin"),
    embedGenerateIndex := true,
    embedDirectories ++= (Compile / unmanagedSourceDirectories).value,
    (Compile / sourceGenerators) += embedFiles
  )
