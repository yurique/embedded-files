package com.yurique.embedded

import org.junit.Test
import org.junit.Assert._

class TextTest {

  @Test def textFile(): Unit = {
    assertEquals(
      "can read text file",
      "test string",
      FileAsString("/test/files/test text")
    )
  }

}
