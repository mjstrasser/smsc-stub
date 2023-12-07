//import com.github.retronym.SbtOneJar._

//oneJarSettings

lazy val commonSettings = Seq(
  organization := "michaelstrasser.com",
  version := "0.0.6",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "SMSC Stub Server in Akka"
  )

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

libraryDependencies ++= {
  val akkaV = "2.3.12"
  val sprayV = "1.3.3"
  Seq(
    "com.typesafe.akka" %% "akka-actor"       % akkaV,
    "com.typesafe.akka" %% "akka-slf4j"       % akkaV,
    "io.spray"          %% "spray-can"        % sprayV,
    "io.spray"          %% "spray-routing"    % sprayV,
    "ch.qos.logback"    %  "logback-classic"  % "1.4.14",
    "org.scalatest"     %% "scalatest"        % "2.2.1"    % Test
  )
}
