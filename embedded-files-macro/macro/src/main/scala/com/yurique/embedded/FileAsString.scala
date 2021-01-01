package com.yurique.embedded

import java.io.File
import scala.reflect.macros.blackbox

object FileAsString {

  var rootPackage: String = "__embedded_files"
  def apply(fileName: String): String = macro FileAsStringImpl.referEmbeddedFile

}

class FileAsStringImpl(val c: blackbox.Context) {
  import c._
  import universe._

  def referEmbeddedFile(fileName: c.Expr[String]) = fileName.tree match {
    case Literal(Constant(fileNameStr: String)) =>
      val file = {
        if (fileNameStr.startsWith("/")) {
          new File(fileNameStr)
        } else {
          new File(new File(c.enclosingPosition.source.path).getParentFile.getAbsolutePath.split('/').dropWhile(_ != "scala").drop(1).mkString("/"), fileNameStr)
        }
      }.toPath

      val packageName: String = FileAsString.rootPackage + "." + file.getParent.toString.replace("/", ".").dropWhile(_ == '.')

      val className: String =
        s"${file.getFileName.toString.replaceAll("\\W", "_").replaceAll("_+", "_")}"

      val maybeSelectObject = packageName
        .split('.').foldLeft[Option[c.Tree]](
          None
        ) { (chain, next) =>
          chain match {
            case None        => Some(Ident(TermName(next)))
            case Some(chain) => Some(Select(chain, TermName(next)))
          }
        }

      maybeSelectObject
        .map { selectObject =>
          Select(Select(selectObject, TermName(className)), TermName("content"))
        }.getOrElse(throw new RuntimeException(s"invalid package and class: ${packageName} ${className} for file ${fileNameStr}"))
  }

}
