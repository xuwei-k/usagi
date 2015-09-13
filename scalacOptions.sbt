val unusedWarnings = (
  "-Ywarn-unused" ::
  "-Ywarn-unused-import" ::
  Nil
)

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-Xlint" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  Nil
) ::: (
  if(scalaVersion.value.startsWith("2.10")) Nil else unusedWarnings
)

Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) ~= {_.filterNot(unusedWarnings.toSet)}
)
