name := "thunderbolt"

version := "1.0"

lazy val `thunderbolt` = (project in file(".")).enablePlugins(PlayScala).dependsOn(
  RootProject(uri("git://github.com/dexmo007/XMaths-lib.git"))
)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(jdbc, cache, ws, specs2 % Test,
  "org.sorm-framework" % "sorm" % "0.3.19",
  "de.hd" % "jsonclassgenerator" % "1.1",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.apache.commons" % "commons-math3" % "3.2"
)

dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += Resolver.mavenLocal