

scalaVersion := "2.12.8"

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

scalacOptions in Test ++= Seq("-Yrangepos")

libraryDependencies += "com.google.cloud" % "google-cloud-speech" % "0.80.0-beta"

mainClass in assembly := Some("com.jsuereth.server.ir.TestCamera")
assemblyJarName in assembly := "wand.jar"



