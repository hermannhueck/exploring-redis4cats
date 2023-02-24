// Hashes API

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val commandsApi: Resource[IO, HashCommands[IO, String, String]] =
  Redis[IO].utf8("redis://localhost")

implicit val runtime = cats.effect.unsafe.IORuntime.global

val testKey   = "foo1"
val testField = "bar1"

def putStrLn(str: String): IO[Unit] = IO(println(str))

val showResult: Option[String] => IO[Unit] =
  _.fold(putStrLn(s"Not found key: $testKey | field: $testField"))(s => putStrLn(s))

commandsApi
  .use { redis => // HashCommands[IO, String, String]
    for {
      x <- redis.hGet(testKey, testField)
      _ <- showResult(x)
      _ <- redis.hSet(testKey, testField, "some value")
      y <- redis.hGet(testKey, testField)
      _ <- showResult(y)
      _ <- redis.hSetNx(testKey, testField, "should not happen")
      w <- redis.hGet(testKey, testField)
      _ <- showResult(w)
      _ <- redis.hDel(testKey, testField)
      z <- redis.hGet(testKey, testField)
      _ <- showResult(z)
    } yield ()
  }
  .unsafeRunSync()
