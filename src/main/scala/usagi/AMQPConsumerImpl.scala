package usagi

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.{Envelope, ShutdownSignalException, Consumer => AMQPConsumer}

final class AMQPConsumerImpl(callback: ConsumeMessage => Unit) extends AMQPConsumer{
  override def handleConsumeOk(consumerTag: String): Unit =
    callback(ConsumeMessage.ConsumeOk(consumerTag))

  override def handleCancel(consumerTag: String): Unit =
    callback(ConsumeMessage.Cancel(consumerTag))

  override def handleRecoverOk(consumerTag: String): Unit =
    callback(ConsumeMessage.RecoverOk(consumerTag))

  override def handleCancelOk(consumerTag: String): Unit =
    callback(ConsumeMessage.CancelOk(consumerTag))

  override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]): Unit =
    callback(ConsumeMessage.Delivery(consumerTag, envelope, properties, body))

  override def handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException): Unit =
    callback(ConsumeMessage.ShutdownSignal(consumerTag, sig))
}
