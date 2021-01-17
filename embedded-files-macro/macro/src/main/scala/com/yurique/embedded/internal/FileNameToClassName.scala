package com.yurique.embedded.internal

private[embedded] object FileNameToClassName {

  private def toValidTerm(s: String): String =
    s.trim.replaceAll("\\W", "_").replaceAll("_+", "_")

  def apply(
      fileName: String,
      sourceFileName: String,
      rootPackage: String
  ): Either[String, Seq[String]] = {
    println(s"fileName: $fileName")
    println(s"sourceFileName: $sourceFileName")

    val filePath =
      fileName.trim.split("/").filterNot(_.isEmpty).map(toValidTerm)
    if (filePath.isEmpty) {
      Left(s"invalid file name: ${fileName}")
    } else {
      val className = filePath.last
      val packagePath =
        if (fileName.startsWith("/")) {
          filePath.dropRight(1)
        } else {
          val sourcePath       = sourceFileName.replace("\\", "/").split("/")
          val sourceParentPath = sourcePath.dropRight(1)
          val sourceRelativePath = sourceParentPath
            .dropWhile(!_.startsWith("scala"))
            .drop(1)
            .map(toValidTerm)
          sourceRelativePath ++ filePath.dropRight(1)
        }
      Right(
        Seq.concat(
          Seq(rootPackage),
          packagePath,
          Seq(className)
        )
      )
    }
  }

}
