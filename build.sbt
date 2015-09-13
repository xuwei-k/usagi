scalaVersion := "2.11.7"

name := "usagi"

fork in run := true

libraryDependencies += "org.scalaz.stream" %% "scalaz-stream" % "0.7.3a"
libraryDependencies += "com.rabbitmq" % "amqp-client" % "3.5.4"
