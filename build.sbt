Global / onChangedBuildSource := ReloadOnSourceChanges

name := "scala-http-docker"
version := "0.0.1"

scalaVersion := "2.13.8"

javacOptions := Seq(
  "-source",
  "11",
  "-target",
  "11"
)

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:existentials",
  "-Wconf:cat=other-match-analysis:error",
  "-Xfatal-warnings",
  "-Ymacro-annotations",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-Yrepl-class-based"
)

def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % "1.0.0-M32"
def circe(artifact: String): ModuleID = "io.circe"    %% artifact % "0.14.1"

libraryDependencies ++= Seq(
  compilerPlugin("org.typelevel"  %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
  compilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.1"),
  compilerPlugin("org.augustjune" %% "context-applied"    % "0.1.4"),
  http4s("http4s-dsl"),
  http4s("http4s-blaze-server"),
  http4s("http4s-circe"),
  circe("circe-generic-extras")
)

enablePlugins(JavaAppPackaging, DockerPlugin)
dockerBaseImage := "openjdk:8-jre-slim"
dockerExposedPorts ++= Seq(8080)
