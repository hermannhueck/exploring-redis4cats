// Codecs
//=========

// ----- Compression

import dev.profunktor.redis4cats.data.RedisCodec

RedisCodec.deflate(RedisCodec.Utf8)
RedisCodec.gzip(RedisCodec.Utf8)

// ----- Encryption

import cats.effect._
import javax.crypto.spec.SecretKeySpec

def mkCodec(key: SecretKeySpec): IO[RedisCodec[String, String]] =
  for {
    e <- RedisCodec.encryptSupplier[IO](key)
    d <- RedisCodec.decryptSupplier[IO](key)
  } yield RedisCodec.secure(RedisCodec.Utf8, e, d)

// ----- Deriving codecs

// Split Epimorphism
// final case class SplitEpi[A, B](
//     get: A => B,
//     reverseGet: B => A
// ) extends (A => B)

import dev.profunktor.redis4cats.codecs.splits._
import scala.util.Try

val stringDoubleEpi: SplitEpi[String, Double] =
  SplitEpi(s => Try(s.toDouble).getOrElse(0.0), _.toString)

val stringLongEpi: SplitEpi[String, Long] =
  SplitEpi(s => Try(s.toLong).getOrElse(0L), _.toString)

val stringIntEpi: SplitEpi[String, Int] =
  SplitEpi(s => Try(s.toInt).getOrElse(0), _.toString)

import dev.profunktor.redis4cats.codecs.Codecs

val longCodec: RedisCodec[String, Long] =
  Codecs.derive(RedisCodec.Utf8, stringLongEpi)

// ----- Json codecs

sealed trait Event

object Event {
  case class Ack(id: Long)                      extends Event
  case class Message(id: Long, payload: String) extends Event
  case object Unknown                           extends Event
}

import io.circe.generic.auto._
import io.circe.parser.{decode => jsonDecode}
import io.circe.syntax._

val eventSplitEpi: SplitEpi[String, Event] =
  SplitEpi[String, Event](
    str => jsonDecode[Event](str).getOrElse(Event.Unknown),
    ev => ev.asJson.noSpaces
  )

val eventsCodec: RedisCodec[String, Event] =
  Codecs.derive(RedisCodec.Utf8, eventSplitEpi)

import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.NoOp._

val eventsKey = "events"

implicit val runtime = cats.effect.unsafe.IORuntime.global

Redis[IO]
  .simple("redis://localhost", eventsCodec)
  .use { redis =>
    for {
      x <- redis.sCard(eventsKey)
      _ <- IO(println(s"Number of events: $x"))
      _ <- redis.sAdd(eventsKey, Event.Ack(1), Event.Message(23, "foo"))
      y <- redis.sMembers(eventsKey)
      _ <- IO(println(s"Events: $y"))
    } yield ()
  }
  .unsafeRunSync()
