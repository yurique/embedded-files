package com.yurique.embedded.sbt

import sbt._
import sbt.Keys._

import java.nio.file.Files
import java.nio.file.Path
import sbt.nio.Keys._

object EmbeddedFilesPlugin extends AutoPlugin {

  override def trigger = noTrigger

  object autoImport {

    case class TransformConfig(
        when: Path => Boolean,
        transform: String => String
    )

    val embedFiles = taskKey[Seq[File]]("Creates an ExternalFile object with a content field for each file.")
    val embedRootPackage = settingKey[String]("Root package for generated classes (default: __embedded_files)")
    val embedDirectories = settingKey[Seq[File]]("Directories to look for files in (default: Compile / unmanagedResourceDirectories)")
    val embedTextGlobs = settingKey[Seq[String]]("glob patterns for text files (default: Seq(**/*.txt))")
    val embedBinGlobs = settingKey[Seq[String]]("glob patterns for binary files (default: Seq.empty)")
    val embedGenerateIndex = settingKey[Boolean]("Whether or not to generate the EmbeddedFilesIndex object (default: false)")
    val embedTransform = settingKey[Seq[TransformConfig]]("Transform configuration (markdown -> html, etc")
  }

  import autoImport._

  private def toValidId = (s: String) => s.trim.replaceAll("\\W", "_").replaceAll("_+", "_")

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    embedRootPackage := "__embedded_files",
    embedDirectories := (Compile / unmanagedResourceDirectories).value,
    embedTextGlobs := Seq("**/*.txt"),
    embedBinGlobs := Seq.empty,
    embedGenerateIndex := false,
    embedTransform := Seq.empty,
    embedFiles / fileInputs ++= embedDirectories.value.flatMap(embedDirectory =>
      embedTextGlobs.value.map(glob => embedDirectory.toGlob / glob)
    ),
    embedFiles / fileInputs ++= embedDirectories.value.flatMap(embedDirectory =>
      embedBinGlobs.value.map(glob => embedDirectory.toGlob / glob)
    ),
    embedFiles := {
      val rootPackage = embedRootPackage.value

      val outputDir = (Compile / sourceManaged).value.toPath / "scala"

      def packageName(relative: Path): String = {
        if (relative.getParent != null) {
          var subPackage = relative.getParent.toString.split('/').map(toValidId).mkString(".")
          if (subPackage.headOption.exists(_.isDigit)) {
            subPackage = "_" + subPackage
          }
          if (subPackage.isEmpty) {
            s"${rootPackage}"
          } else {
            s"${rootPackage}.${subPackage}"
          }
        } else {
          s"${rootPackage}"
        }
      }

      def className(relative: Path): String = {
        s"${toValidId(relative.getFileName.toString)}"
      }

      def outputPath(packageName: String, className: String): Path = {
        outputDir / packageName.replaceAllLiterally(".", "/") / s"${className}.scala"
      }

      val sourceMap =
        embedFiles.inputFiles.view.flatMap { path =>
          embedDirectories.value
            .flatMap(path.toFile.relativeTo)
            .headOption
            .map(_.toPath)
            .map { relative =>
              outputPath(packageName(relative), className(relative)) -> path
            }
        }.toMap

      val existingTargets =
        fileTreeView.value
          .list(outputDir.toGlob / **)
          .flatMap { case (p, _) =>
            if (p.toFile.isFile && !sourceMap.contains(p)) {
              Files.deleteIfExists(p)
              None
            } else {
              Some(p)
            }
          }
          .toSet

      val changes = embedFiles.inputFileChanges
      val updatedPaths = (changes.created ++ changes.modified).toSet
      val needToEmbed =
        updatedPaths ++ sourceMap.filterKeys(!existingTargets(_)).values

      val transform = embedTransform.value

      needToEmbed.foreach { path =>
        embedDirectories.value
          .flatMap(path.toFile.relativeTo)
          .headOption
          .map(_.toPath)
          .foreach { relative =>
            val isBinary =
              embedDirectories.value
                .flatMap(embedDirectory => embedBinGlobs.value.map(glob => embedDirectory.toGlob / glob))
                .exists(_.matches(path))

            val filePackageName = packageName(relative)
            val fileClassName = className(relative)
            val fileOutputPath = outputPath(filePackageName, fileClassName)
            if (isBinary) {
              buildEmbeddedBinFile(
                input = path,
                relative = relative,
                output = fileOutputPath,
                rootPackage = rootPackage,
                packageName = filePackageName,
                className = fileClassName
              )
            } else {
              buildEmbeddedTextFile(
                input = path,
                relative = relative,
                output = fileOutputPath,
                rootPackage = rootPackage,
                packageName = filePackageName,
                className = fileClassName,
                transform = transform
              )
            }
          }
      }
      if (embedGenerateIndex.value) {
        val generatedClasses = sourceMap.values.flatMap { path =>
          embedDirectories.value
            .flatMap(path.toFile.relativeTo)
            .headOption
            .map(_.toPath)
            .map { relative =>
              val isBinary =
                embedDirectories.value
                  .flatMap(embedDirectory =>
                    embedBinGlobs.value
                      .map(glob => embedDirectory.toGlob / glob)
                  )
                  .exists(_.matches(path))

              isBinary -> (relative, packageName(relative), className(
                relative
              ))
            }
        }
        buildIndexFile(
          textInputs = generatedClasses.toSeq
            .collect { case (false, generatedClassInfo) =>
              generatedClassInfo
            }
            .sortBy(_.toString()),
          binInputs = generatedClasses.toSeq
            .collect { case (true, generatedClassInfo) =>
              generatedClassInfo
            }
            .sortBy(_.toString()),
          output = outputPath(rootPackage, "EmbeddedFilesIndex"),
          packageName = rootPackage
        )
      }
      buildTextFileInterface(
        output = outputPath(rootPackage, "EmbeddedTextFile"),
        packageName = rootPackage
      )
      buildBinFileInterface(
        output = outputPath(rootPackage, "EmbeddedBinFile"),
        packageName = rootPackage
      )

      val generatedFiles = sourceMap.keys.toVector.map(_.toFile) ++
        Vector(
          outputPath(rootPackage, "EmbeddedTextFile").toFile,
          outputPath(rootPackage, "EmbeddedBinFile").toFile
        ) ++
        Vector(
          outputPath(rootPackage, "EmbeddedFilesIndex").toFile
        ).filter(_ => embedGenerateIndex.value)

      generatedFiles
    }
  )

  def buildEmbeddedTextFile(
      input: Path,
      relative: Path,
      output: Path,
      rootPackage: String,
      packageName: String,
      className: String,
      transform: Seq[TransformConfig]
  ): Unit = {

    def doTransform(s: String): String =
      transform.find(_.when(input)).fold(s)(_.transform(s))

    val fileContent =
      doTransform(IO.read(input.toFile))
        .replaceAllLiterally("\"\"\"", "\\\"\\\"\\\"")

    val str =
      s"""|package $packageName
          |
          |object $className extends ${rootPackage}.EmbeddedTextFile {
          |
          |  val path: String = \"\"\"${relative.toString.replaceAllLiterally(
           "\"\"\"",
           "\\\"\\\"\\\""
         )}\"\"\"
          |
          |  val content: String = \"\"\"$fileContent\"\"\"
          |
          |}
          |""".stripMargin
    IO.write(
      output.toFile,
      str
    )
  }

  def buildEmbeddedBinFile(
      input: Path,
      relative: Path,
      output: Path,
      rootPackage: String,
      packageName: String,
      className: String
  ): Unit = {
    val str =
      s"""|package $packageName
          |
          |object $className extends ${rootPackage}.EmbeddedBinFile {
          |
          |  val path: String = \"\"\"${relative.toString.replaceAllLiterally(
           "\"\"\"",
           "\\\"\\\"\\\""
         )}\"\"\"
          |
          |  //format: off
          |  val content: Array[Byte] = Array(
          |    ${IO
           .readBytes(input.toFile)
           .grouped(16)
           .map(
             _.map(_.toHexString.reverse.padTo(2, '0').reverse)
               .map("0x" + _)
               .mkString(", ")
           )
           .mkString(",\n    ")}
          |  )
          |
          |}
          |""".stripMargin
    IO.write(
      output.toFile,
      str
    )
  }

  def buildTextFileInterface(output: Path, packageName: String): Unit = {
    val str =
      s"""|package $packageName
          |
          |abstract class EmbeddedTextFile {
          |  def path: String
          |  def content: String
          |}
          |""".stripMargin
    IO.write(
      output.toFile,
      str
    )
  }

  def buildBinFileInterface(output: Path, packageName: String): Unit = {
    val str =
      s"""|package $packageName
          |
          |abstract class EmbeddedBinFile {
          |  def path: String
          |  def content: Array[Byte]
          |}
          |""".stripMargin
    IO.write(
      output.toFile,
      str
    )
  }

  def buildIndexFile(
      textInputs: Seq[(Path, String, String)],
      binInputs: Seq[(Path, String, String)],
      output: Path,
      packageName: String
  ): Unit = {
    val str =
      s"""|package $packageName
          |
          |object EmbeddedFilesIndex {
          |  val textFiles: Seq[(String, EmbeddedTextFile)] = Seq(
          |    ${textInputs
           .map { case (path, packageName, className) =>
             s""""${path.toString}" -> ${packageName}.${className}"""
           }
           .mkString(",\n    ")}
          |  )
          |  val binFiles: Seq[(String, EmbeddedBinFile)] = Seq(
          |    ${binInputs
           .map { case (path, packageName, className) =>
             s""""${path.toString}" -> ${packageName}.${className}"""
           }
           .mkString(",\n    ")}
          |  )
          |}
          |""".stripMargin
    IO.write(
      output.toFile,
      str
    )
  }

}
