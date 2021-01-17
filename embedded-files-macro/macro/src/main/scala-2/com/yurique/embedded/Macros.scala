package com.yurique.embedded

object EmbeddedFilesConfig {

  var rootPackage: String = "__embedded_files"

}

object FileAsString {

  def apply(fileName: String): String =
    macro ConetentRefImpl.referEmbeddedFile

}

object FileAsByteArray {

  def apply(fileName: String): Array[Byte] =
    macro ConetentRefImpl.referEmbeddedFile

}
