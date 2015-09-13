package usagi

import com.rabbitmq.client.{BasicProperties, Envelope, ShutdownSignalException}

sealed abstract class ConsumeMessage extends Product with Serializable

object ConsumeMessage{
  final case class ConsumeOk(tag: String) extends ConsumeMessage
  final case class Cancel(tag: String) extends ConsumeMessage
  final case class RecoverOk(tag: String) extends ConsumeMessage
  final case class CancelOk(tag: String) extends ConsumeMessage
  final case class Delivery(tag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]) extends ConsumeMessage
  final case class ShutdownSignal(tag: String, signal: ShutdownSignalException) extends ConsumeMessage
}
