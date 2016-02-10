import sbt._, Keys._

object build extends Build {

  private[this] val usagiName = "usagi"
  val modules = usagiName :: Nil

  lazy val usagi = Project(
    usagiName, file(".")
  ).settings(
    Common.commonSettings ++ xerial.sbt.Sonatype.sonatypeRootSettings
  ).settings(
    name := usagiName,
    fork in run := true,
    libraryDependencies += "org.scalaz.stream" %% "scalaz-stream" % "0.8a",
    libraryDependencies += "com.rabbitmq" % "amqp-client" % "3.6.0"
  )

}
