import build._

Common.commonSettings

name := usagiName
fork in run := true
libraryDependencies += "org.scalaz.stream" %% "scalaz-stream" % "0.8.6a"
libraryDependencies += "com.rabbitmq" % "amqp-client" % "4.1.0"
