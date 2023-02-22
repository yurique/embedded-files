import laika.api.Transformer
import laika.ast.Path.Root
import laika.format.{AST, HTML, Markdown, ReStructuredText, XSLFO}
import laika.markdown.bundle.VerbatimHTML
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import laika.rewrite.link.LinkConfig


version := "0.1"
scalaVersion := "2.13.6"

val root = project
  .in(file("."))
  .enablePlugins(EmbeddedFilesPlugin)
  .settings(
    embedTextGlobs := Seq("**/*.txt", "**/*.md"),
    embedBinGlobs := Seq("**/*.bin"),
    embedDirectories ++= (Compile / unmanagedSourceDirectories).value,
    embedTransform := Seq(
      TransformConfig(
        when = _.getFileName.toString.endsWith(".md"),
        transformer = Transformer.from(Markdown).to(HTML).using(GitHubFlavor, VerbatimHTML, SyntaxHighlighting).build
      )
    ),
    (Compile / sourceGenerators) += embedFiles
  )
