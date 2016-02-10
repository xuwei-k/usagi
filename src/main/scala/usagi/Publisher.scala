package usagi

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.{ConfirmListener, Channel => AMQPChannel}
import scodec.bits.ByteVector
import scala.util.control.NonFatal
import scalaz._
import scalaz.concurrent.Task
import scalaz.stream._

final case class Publisher[A](
  publishSink: Sink[Task, Publisher.Publish[A]],
  confirmResult: Process[Task, Publisher.Response[A]]
)

object Publisher {

  final case class Publish[A](id: A, bytes: ByteVector)

  sealed abstract class Response[A] extends Product with Serializable

  final case class Ack[A](values: List[A]) extends Response[A]
  final case class Nack[A](values: List[A]) extends Response[A]
  final case class IdNotFound[A](ack: List[A], nack: List[A], tags: NonEmptyList[Long]) extends Response[A]

  def of(
    exchange: String,
    routingKey: String,
    mandatory: Boolean,
    immediate: Boolean,
    props: BasicProperties
  ): Reader[AMQPChannel, Forall[Publisher]] = Reader { channel =>

    new Forall[Publisher] {
      def apply[A] = {

        channel.queueDeclare(routingKey, true, false, false, null)
        channel.confirmSelect()

        val queue = scalaz.stream.async.unboundedQueue[Response[A]]
        val messages = collection.concurrent.TrieMap.empty[Long, A]
        val firstTagNumber = channel.getNextPublishSeqNo

        val closeProcess = Process.eval_{
          Task[Unit] {
            if (channel.isOpen) {
              channel.close()
            }
          }
        }

        val confirm: Process[Task, Response[A]] = queue.dequeue.onComplete(closeProcess)

        channel.addConfirmListener(new ConfirmListener {
          @volatile private[this] var lastTag = firstTagNumber

          private[this] def handle(tag: Long, multiple: Boolean, isAck: Boolean) = try {
            val range = synchronized {
              val r = if (multiple) {
                (lastTag + 1) to tag
              } else {
                tag to tag
              }
              lastTag = tag
              r
            }

            val (notFounds, ids) = range.reverseIterator.foldLeft(List.empty[Long] -> List.empty[A]) {
              case ((notFound, acc), t) =>
                messages.remove(t) match {
                  case Some(id) =>
                    notFound -> (id :: acc)
                  case None =>
                    (t :: notFound) -> acc
                }
            }

            val result = scalaz.std.list.toNel(notFounds) match {
              case Some(notFound) =>
                if (isAck) {
                  IdNotFound(ids, Nil, notFound)
                } else {
                  IdNotFound(Nil, ids, notFound)
                }
              case None =>
                if (isAck) {
                  Ack(ids)
                } else {
                  Nack(ids)
                }
            }
            queue.enqueueOne(result).unsafePerformSync
          } catch {
            case NonFatal(e) =>
              e.printStackTrace()
          }

          override def handleAck(tag: Long, multiple: Boolean): Unit =
            handle(tag, multiple, true)

          override def handleNack(tag: Long, multiple: Boolean): Unit =
            handle(tag, multiple, false)
        })

        val publisher: Sink[Task, Publish[A]] = stream.sink.lift((publish: Publish[A]) =>
          Task.now[Unit] {
            val tag = channel.synchronized {
              val t = channel.getNextPublishSeqNo
              channel.basicPublish(exchange, routingKey, mandatory, immediate, props, publish.bytes.toArray)
              t
            }
            messages.put(tag, publish.id)
          }
        )

        Publisher(publisher, confirm)
      }
    }
  }
}
