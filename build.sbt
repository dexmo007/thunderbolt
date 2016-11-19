name := "thunderbolt"

version := "1.0"

lazy val `thunderbolt` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq( jdbc , cache , ws   , specs2 % Test)

libraryDependencies += "org.sorm-framework" % "sorm" % "0.3.19"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.36"

dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  