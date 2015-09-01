import com.github.retronym.SbtOneJar._

oneJarSettings

lazy val commonSettings = Seq(
  organization := "michaelstrasser.com",
  version := "0.0.2",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "SMSC Stub Server in Akka"
  )
  
libraryDependencies ++= {
  val akkaV = "2.3.12"
  val sprayV = "1.3.3"
  Seq(
    "com.typesafe.akka" %% "akka-actor"       % akkaV,
    "com.typesafe.akka" %% "akka-slf4j"       % akkaV,
    "io.spray"          %% "spray-can"        % sprayV,
    "io.spray"          %% "spray-routing"    % sprayV,
    "ch.qos.logback"    %  "logback-classic"  % "1.1.3",
    "org.scalatest"     %% "scalatest"        % "2.2.1"    % Test
  )
}
