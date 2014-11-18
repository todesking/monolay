name := "monolay"

version := "0.0.1-SNAPSHOT"

organization := "com.todesking"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
)

scalacOptions ++= Seq("-deprecation", "-feature")

publishTo := Some(Resolver.file("com.todesking",file("./repo/"))(Patterns(true, Resolver.mavenStyleBasePattern)))

// scoverage
instrumentSettings

ScoverageKeys.highlighting := true

// ScalaTest: Generate junit-style xml report
testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/reports")
