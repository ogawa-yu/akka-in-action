import scala.collection.JavaConverters._

enablePlugins(JavaServerAppPackaging)

name := "goticks"
version := "1.0"
organization := "com.goticks"
scalaVersion := "2.12.4"

libraryDependencies ++= {
  val akkaVersion = "2.5.4"
  val akkaHttpVersion = "10.1.0-RC1"
  val akkaNs = "com.typesafe.akka"
  Seq(
    akkaNs %% "akka-actor" % akkaVersion,
    akkaNs %% "akka-stream" % akkaVersion,
    akkaNs %% "akka-slf4j" % akkaVersion,
    akkaNs %% "akka-testkit" % akkaVersion % "test",

    akkaNs %% "akka-remote" % akkaVersion,
    akkaNs %% "akka-multi-node-testkit" % akkaVersion % "test",

    akkaNs %% "akka-http-core" % akkaHttpVersion,
    akkaNs %% "akka-http" % akkaHttpVersion,
    akkaNs %% "akka-http-spray-json" % akkaHttpVersion,
    akkaNs %% "akka-http-jackson" % akkaHttpVersion,
    akkaNs %% "akka-http-testkit" % akkaHttpVersion % "test",

    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "org.projectlombok" % "lombok" % "1.16.16",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "junit" % "junit" % "4.12" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test"
  )
}

// Assembly settings
assemblyJarName in assembly := "goticks-server.jar"

