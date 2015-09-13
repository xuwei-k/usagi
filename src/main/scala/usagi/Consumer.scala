package usagi

import com.rabbitmq.client.{Channel => AMQPChannel}
import scala.collection.convert.decorateAsJava._
import scalaz._
import scalaz.concurrent.Task
import scalaz.stream._

final case class Consumer(
  consume: Process[Task, ConsumeMessage],
  response: Sink[Task, ConsumeResult]
)

object Consumer {

  def autoAck(setting: ConsumerSetting): Reader[AMQPChannel, Process[Task, ConsumeMessage]] = Reader{ channel =>
    val queue = async.unboundedQueue[ConsumeMessage]

    channel.basicConsume(
      setting.queue,
      true,
      setting.consumerTag,
      setting.nonLocal,
      setting.exclusive,
      setting.arguments.asJava,
      new AMQPConsumerImpl( message =>
        queue.enqueueOne(message).run
      )
    )

    queue.dequeue.onComplete(
      Process.eval_(Task(channel.close()))
    )
  }

  def of(setting: ConsumerSetting): Reader[AMQPChannel, Consumer] = Reader{ channel =>
    val consume = autoAck(setting).run(channel)

    val response = sink.lift[Task, ConsumeResult]{
      case ConsumeResult.Ack(values) => Task{
        values.foreach(tag =>
          channel.basicAck(tag, false)
        )
      }
      case ConsumeResult.Nack(values) => Task{
        values.foreach{ case (tag, requeue) =>
          channel.basicNack(tag, false, requeue)
        }
      }
    }

    Consumer(consume, response)
  }

}
