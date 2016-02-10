package usagi

import java.nio.charset.StandardCharsets

import com.rabbitmq.client.ConnectionFactory
import scodec.bits.ByteVector

import scala.concurrent.duration._
import scala.util.control.NonFatal
import scalaz.concurrent.Task
import scalaz.stream.Process

object Main {

  final val queueName = "hoge"

  val printResult = scalaz.stream.io.stdOutLines.contramap[Any](_.toString)

  type MessageId = Int

  val intToPublish: Int => Publisher.Publish[Int] = { i =>
    Publisher.Publish(i, ByteVector(i.toString.getBytes(StandardCharsets.UTF_8)))
  }

  val size = 100

  val source: Process[Task, Publisher.Publish[Int]] =
    scalaz.stream.Process.iterate(0)(_ + 1).map(intToPublish).take(size)

  val consumerSetting = ConsumerSetting(
    queueName, "", false, false, Map.empty
  )

  def main (args: Array[String]): Unit = {

    val factory = new ConnectionFactory
    val connection = factory.newConnection()
    val publisher = Publisher.of(
      "", queueName, false, false, null
    ).run(connection.createChannel()).apply[MessageId]

    try {
      val result = (for {
        _ <- source.to(publisher.publishSink).run
        _ <- publisher.confirmResult.observe(printResult).kill.run
        consumeResult <- Consumer.autoAck(consumerSetting).run(connection.createChannel()).collect {
          case d: ConsumeMessage.Delivery => new String(d.body, StandardCharsets.UTF_8)
        }.observe(printResult).take(size).runLog
      } yield consumeResult).onFinish{ err =>
        Task {
          println("onFinish error = " + err)
          println("close connection")
          connection.close()
        }
      }.unsafePerformSyncFor(10.second)

      println("result = " + result)
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        sys.exit()
    }
  }

}
