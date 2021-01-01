package __embedded_files.com.yurique.embedded.sbt.txt

object test_file_1_txt extends __embedded_files.EmbeddedTextFile {

  val path: String = """com/yurique/embedded/sbt/txt/test_file_1.txt"""

  val content: String = """A test file.

Should be embedded.

Triple " should be \"\"\"handled\"\"\".
"""

}
