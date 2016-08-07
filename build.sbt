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
  "com.storm-enroute" %% "reactors" % "0.6")
