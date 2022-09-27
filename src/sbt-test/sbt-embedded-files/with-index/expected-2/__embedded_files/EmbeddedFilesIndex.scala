package __embedded_files

object EmbeddedFilesIndex {
  val textFiles: Seq[(String, EmbeddedTextFile)] = Seq(
    "com/yurique/embedded/sbt/txt/test_file_1.txt" -> __embedded_files.com.yurique.embedded.sbt.txt.test_file_1_txt,
    "file-to-add.txt" -> __embedded_files.file_to_add_txt,
    "test-resource.txt" -> __embedded_files.test_resource_txt
  )
  val binFiles: Seq[(String, EmbeddedBinFile)] = Seq(
    "com/yurique/embedded/sbt/binary/test_bin_file_1.bin" -> __embedded_files.com.yurique.embedded.sbt.binary.test_bin_file_1_bin,
    "file-to-add.bin" -> __embedded_files.file_to_add_bin,
    "test/test-bin-resource.bin" -> __embedded_files.test.test_bin_resource_bin
  )
}
