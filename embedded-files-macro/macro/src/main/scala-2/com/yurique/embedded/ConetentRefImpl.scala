package com.yurique.embedded

import internal.FileNameToClassName
import scala.reflect.macros.blackbox

class ConetentRefImpl(val c: blackbox.Context) {
  import c._
  import universe._

  def referEmbeddedFile(fileName: c.Expr[String]) = fileName.tree match {
    case Literal(Constant(fileName: String)) =>
      FileNameToClassName(
        fileName,
        c.enclosingPosition.source.path,
        EmbeddedFilesConfig.rootPackage
      ) match {
        case Left(error) =>
          c.abort(c.enclosingPosition, error)
        case Right(classPath) =>
          (classPath ++ Seq("content"))
            .foldLeft[Option[c.Tree]](
              None
            ) { (chain, next) =>
              chain match {
                case None        => Some(Ident(TermName(next)))
                case Some(chain) => Some(Select(chain, TermName(next)))
              }
            }
            .getOrElse(
              c.abort(
                c.enclosingPosition,
                s"invalid select path: ${classPath ++ Seq("content")}"
              )
            )
      }
  }

}
