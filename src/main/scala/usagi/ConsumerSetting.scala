package usagi

final case class ConsumerSetting(
  queue: String,
  consumerTag: String,
  nonLocal: Boolean,
  exclusive: Boolean,
  arguments: Map[String, AnyRef]
)
