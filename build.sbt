import build._

Common.commonSettings

xerial.sbt.Sonatype.sonatypeRootSettings

name := usagiName
fork in run := true
libraryDependencies += "com.rabbitmq" % "amqp-client" % "3.6.5"
libraryDependencies += "co.fs2" %% "fs2-core" % "0.9.2"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.7"
