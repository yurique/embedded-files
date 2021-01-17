package com.yurique.embedded

import scala.quoted._

object FileAsString:

  var rootPackage: String = "__embedded_files"

  inline def apply(inline fileName: String): String = ${ fileAsStringImpl('{fileName}) }
  
  def fileAsStringImpl(e: Expr[String])(using Quotes): Expr[String] = 
    import quotes.reflect._
    val fileName = e.valueOrError
    val sourceFile = Position.ofMacroExpansion.sourceFile.jpath.toString
    internal.FileNameToClassName(fileName, sourceFile, rootPackage) match 
      case Left(error)      => report.throwError(error)  
      case Right(classPath) =>
        (classPath ++ Seq("content")).foldLeft[Option[Term]](None) { 
          case (None, next)        => Some(Ref(Symbol.requiredPackage(next)))
          case (Some(chain), next) => Some(Select.unique(chain, next))
        }
        .fold(report.throwError(s"invalid select path: ${classPath ++ Seq("content")}"))(_.asExprOf[String])
    end match
    
object FileAsByteArray:
  var rootPackage: String = "__embedded_files"

  inline def apply(inline fileName: String): Array[Byte] = ${ fileAsByteArrayImpl('{fileName}) }
  
  def fileAsByteArrayImpl(e: Expr[String])(using Quotes): Expr[Array[Byte]] = 
    import quotes.reflect._
    val fileName = e.valueOrError
    val sourceFile = Position.ofMacroExpansion.sourceFile.jpath.toString
    internal.FileNameToClassName(fileName, sourceFile, rootPackage) match 
      case Left(error)      => report.throwError(error)  
      case Right(classPath) => 
        (classPath ++ Seq("content")).foldLeft[Option[Term]](None) { 
          case (None, next)        => Some(Ref(Symbol.requiredPackage(next)))
          case (Some(chain), next) => Some(Select.unique(chain, next))
        }
        .fold(report.throwError(s"invalid select path: ${classPath ++ Seq("content")}"))(_.asExprOf[Array[Byte]])
    end match
