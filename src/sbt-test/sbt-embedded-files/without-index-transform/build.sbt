version := "0.1"
scalaVersion := "2.13.6"

val root = project
  .in(file("."))
  .enablePlugins(EmbeddedFilesPlugin)
  .settings(
    embedTextGlobs := Seq("**/*.txt", "**/*.md"),
    embedBinGlobs := Seq("**/*.bin"),
    embedDirectories ++= (Compile / unmanagedSourceDirectories).value,
    embedTransform := Seq(
      TransformConfig(
        when = _.getFileName.toString.endsWith("test-resource.txt"),
        transform = _.toUpperCase
      )
    ),
    (Compile / sourceGenerators) += embedFiles
  )
