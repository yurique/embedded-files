package __embedded_files

object file_to_add_bin extends __embedded_files.EmbeddedBinFile {

  val path: String = """file-to-add.bin"""

  //format: off
  val content: Array[Byte] = Array(
    0x42, 0x49, 0x4e
  )

}
