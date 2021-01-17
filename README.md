# embedded-files

An sbt plugin to generate Scala objects containing the contents of glob-specified files as strings or byte-arrays.

The acompanying macro allowing to access those objects more easily: [embedded-files-macro](https://github.com/yurique/embedded-files-macro).

## Installation

### `plugins.sbt`

```scala
addSbtPlugin("com.yurique" % "sbt-embedded-files" % "0.1.2")
```

### `build.sbt`

```scala
 libraryDependencies += "com.yurique" %%% "embedded-files-macro" % "0.1.2"
```

## Example usage

Put a file into `src/main/resources/docs/test.txt`:

```
I'm a test text.
```

Add `embedFiles` to the `Compile / sourceGenerators`:

```scala
  project
    // ...
    .settings(
      (Compile / sourceGenerators) += embedFiles
    )
```

In the code:

```scala
import com.yurique.embedded.FileAsString

val testTxtContents = FileAsString("/docs/test.txt") // "I'm a test text."
```

## Configuration

The sbt plugin has the following configuration keys:

```scala
  project
    // ...
    .settings(
      // default is __embedded_files, which is assumed by the macro
      // the generated objects and classes are put into this package (and sub-packages)
      embedRootPackage := "custom_root_package",

      // a list of directories to look for files to embed in
      // default is (Compile / unmanagedResourceDirectories)
      embedDirectories ++= (Compile / unmanagedSourceDirectories).value,

      // a list of globs for text files
      // default is Seq("**/*.txt")
      embedTextGlobs := Seq("**/*.txt", "**/*.md"),

      // a list of globs for binary files
      // default is Seq.empty
      embedBinGlobs := Seq("**/*.bin"),

      // whether or not to generate a EmbeddedFilesIndex object containing references to all embedded files
      // default is false
      embedGenerateIndex := true,

      // the intended usage is to use the output of the embedFiles task as generated sources
      (Compile / sourceGenerators) += embedFiles
    )
```

## Generated files

### Interfaces

The `embedFiles` always generates these two abstract classes:

```scala
package ${embedRootPackage.value}

abstract class EmbeddedTextFile {
  def path: String
  def content: String
}

```

and

```scala
package ${embedRootPackage.value}

abstract class EmbeddedBinFile {
  def path: String
  def content: Array[Byte]
}
```

### Text files

For each file in the `embedDirectories` that matches any of the `embedTextGlobs` a file like the following is generated:

```scala
package ${embedRootPackage.value}.${subPackage}

object ${className} extends __embedded_files.EmbeddedTextFile {

  val path: String = """path/to/the/file/filename.txt"""

  val content: String = """the content of the file"""

}
```

### Binary files

For each file in the `embedDirectories` that matches any of the `embedBinGlobs` a file like the following is generated:

```scala
package ${embedRootPackage.value}.${subPackage}

object ${className} extends __embedded_files.EmbeddedBinFile {

  val path: String = """path/to/the/file/filename.bin"""

  val content: Array[Byte] = Array(
    // example bytes
    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
  )

}
```

### Package- and class-names

For each file, its path is taken relative to the one of the `embedDirectories` and converted into the package name and the class name.

For example, if the file was `/home/user/.../.../project/src/main/resources/some-dir/1 some sub dir/some things & other things.txt`:

- a relative path is `some-dir/1 some sub dir/some things & other things.txt` (relative to `Compile / unmanagedSourceDirectories` in this example)
- the dirname is `some-dir/1 some sub dir`, it is split by `/`, every part is converted to a valid Scala ID (by replacing non alpha-numerics by `_` and prepending `_` is the path starts with a digit)
- the resulting package name is `some_dir._1_some_sub_dir`
- the class name is derived from the file name: `some_things_other_things`

## Index file

if `embedGenerateIndex` is set to `true`, the index file is generated like the following:

```scala
package ${embedRootPackage.value}

object EmbeddedFilesIndex {
  val textFiles: Seq[(String, EmbeddedTextFile)] = Seq(
    "test/test-resource.txt" -> __embedded_files.test.test_resource_txt,
    "com/company/test_file_1.txt" -> __embedded_files.com.company.test_file_1_txt
  )
  val binFiles: Seq[(String, EmbeddedBinFile)] = Seq(
    "test/test-bin-resource.bin" -> __embedded_files.test.test_bin_resource_bin,
    "com/company/test_bin_file_1.bin" -> __embedded_files.com.company.test_bin_file_1_bin
  )
}
```

## macros

Assuming the `embedFiles` is used as a source generator with the default root package, you can use the macros provided by the `embedded-files-macro` library to get the string/byte-array content of the files like this:

```scala
import com.yurique.embedded._
val s: String = FileAsString("/docs/test.txt")
val b: Array[Byte] = FileAsByteArray("/bins/test.bin")
```

If the path passed to the macros starts with a slash, it is used as is.

If it doesn't start with a slash, the macro does the following:

`/home/user/.../project/src/main/scala/com/company/MyClass.scala`

```scala
package com.company.MyClass

object MyClass {
  val s: String = FileAsString("dir/data.txt")
}
```

Here, the file name doesn't start with a `/` – `dir/data.txt`.

The calling site is in the `/home/user/.../project/src/main/scala/com/company/` directory.

The requested file name is appended to this directory, resulting in `/home/user/.../project/src/main/scala/com/company/dir/data.txt`.

This file is taken relative to the first `scala` directory in the path, resulting in `/com/company/dir/data.txt`.

## Missing embedded files

The macros are not currently doing any checks for whether the embedded files exist. If they don't, the scalac will just fail to compile in a normal way.
