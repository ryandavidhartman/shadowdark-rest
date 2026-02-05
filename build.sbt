ThisBuild / scalaVersion := "2.13.18"

lazy val swaggerVersion = "2.2.42"
lazy val jacksonScalaVersion = "2.19.2"

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "5.6.2",
  "dev.zio" %% "zio" % "2.1.24",
  "dev.zio" %% "zio-http" % "3.7.4",
  "dev.zio" %% "zio-json" % "0.7.44",
  "dev.zio" %% "zio-cache" % "0.2.7",
  "dev.zio" %% "zio-test" % "2.1.24" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.1.24" % Test,
  "com.typesafe" % "config" % "1.4.5",
  "org.apache.pdfbox" % "pdfbox" % "3.0.6",
  "org.locationtech.jts" % "jts-core" % "1.18.1",
  "io.swagger.core.v3" % "swagger-core-jakarta" % swaggerVersion,
  "io.swagger.core.v3" % "swagger-annotations-jakarta" % swaggerVersion,
  "io.swagger.core.v3" % "swagger-models-jakarta" % swaggerVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonScalaVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonScalaVersion
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
