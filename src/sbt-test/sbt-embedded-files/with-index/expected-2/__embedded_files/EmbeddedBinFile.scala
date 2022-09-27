package __embedded_files

abstract class EmbeddedBinFile {
  def path: String
  def content: Array[Byte]
}
