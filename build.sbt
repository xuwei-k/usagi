import build._

Common.commonSettings

xerial.sbt.Sonatype.sonatypeRootSettings

name := usagiName
fork in run := true
libraryDependencies += "org.scalaz.stream" %% "scalaz-stream" % "0.8.6a"
libraryDependencies += "com.rabbitmq" % "amqp-client" % "3.6.5"
