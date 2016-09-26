scalaVersion := "2.11.8"

scalacOptions in Compile ++= Seq(
  "-optimise",
  "-unchecked",
  "-deprecation"
)
resolvers ++= Seq(
  "Sonatype OSS Snapshots" at
    "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at
    "https://oss.sonatype.org/content/repositories/releases")

libraryDependencies ++= Seq(
  "com.storm-enroute" %% "reactors" % "0.7-SNAPSHOT")

libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "3.8.5" % "test")

scalacOptions in Test ++= Seq("-Yrangepos")


// Attempt to control TV, etc.
libraryDependencies ++= Seq("su.litvak.chromecast" % "api-v2" % "0.9.2")
