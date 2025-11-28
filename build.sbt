ThisBuild / scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "5.6.1",
  "dev.zio" %% "zio" % "2.1.23",
  "dev.zio" %% "zio-http" % "3.6.0",
  "dev.zio" %% "zio-json" % "0.7.0",
  "com.typesafe" % "config" % "1.4.3"
)
