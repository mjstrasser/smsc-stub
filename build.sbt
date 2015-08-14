lazy val commonSettings = Seq(
  organization := "michaelstrasser.com",
  version := "0.0.1",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "SMSC Stub Server in Akka IO"
  )

libraryDependencies ++= {
  val akkaVersion = "2.3.12"
  Seq(
    "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
    "org.scalatest"     %% "scalatest"   % "2.2.1"      % "test"
  )
}
