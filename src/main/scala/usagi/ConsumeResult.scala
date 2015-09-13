package usagi

import scalaz.NonEmptyList

sealed abstract class ConsumeResult extends Product with Serializable

object ConsumeResult {
  final case class Ack(values: NonEmptyList[Long]) extends ConsumeResult
  final case class Nack(values: NonEmptyList[(Long, Boolean)]) extends ConsumeResult
}
