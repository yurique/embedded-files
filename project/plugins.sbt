libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.14.0")
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.9")
