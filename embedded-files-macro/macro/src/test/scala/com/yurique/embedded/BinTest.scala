package com.yurique.embedded

import org.junit.Test
import org.junit.Assert._

class BinTest {

  @Test def textFile(): Unit = {
    assertEquals(
      "can read text file",
      Array[Byte](1, 2, 3, 4).toSeq,
      FileAsByteArray("/test/files/test bin").toSeq
    )
  }

}
