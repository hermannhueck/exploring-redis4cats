// Sets API

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration._

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val commandsApi: Resource[IO, SetCommands[IO, String, String]] =
  Redis[IO].utf8("redis://localhost")

implicit val runtime = cats.effect.unsafe.IORuntime.global

val testKey = "foos"

def putStrLn(str: String): IO[Unit] = IO(println(str))

val showResult: Set[String] => IO[Unit] = x => putStrLn(s"$testKey members: $x")

commandsApi
  .use { redis => // SetCommands[IO, String, String]
    for {
      x <- redis.sMembers(testKey)
      _ <- showResult(x)
      _ <- redis.sAdd(testKey, "set value")
      y <- redis.sMembers(testKey)
      _ <- showResult(y)
      _ <- redis.sCard(testKey).flatMap(s => putStrLn(s"size: ${s.toString}"))
      _ <- redis.sRem("non-existing", "random")
      w <- redis.sMembers(testKey)
      _ <- showResult(w)
      _ <- redis.sRem(testKey, "set value")
      z <- redis.sMembers(testKey)
      _ <- showResult(z)
      _ <- redis.sCard(testKey).flatMap(s => putStrLn(s"size: ${s.toString}"))
    } yield ()
  }
  .unsafeRunSync()
