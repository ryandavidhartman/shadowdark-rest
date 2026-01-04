ThisBuild / scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "5.6.2",
  "dev.zio" %% "zio" % "2.1.24",
  "dev.zio" %% "zio-http" % "3.7.4",
  "dev.zio" %% "zio-json" % "0.7.44",
  "dev.zio" %% "zio-cache" % "0.2.7",
  "com.typesafe" % "config" % "1.4.5",
  "org.apache.pdfbox" % "pdfbox" % "3.0.6"
)
