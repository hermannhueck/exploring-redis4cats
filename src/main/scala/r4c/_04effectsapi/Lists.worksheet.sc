// Lists API

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration._

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val commandsApi: Resource[IO, ListCommands[IO, String, String]] =
  Redis[IO].utf8("redis://localhost")

implicit val runtime = cats.effect.unsafe.IORuntime.global

val testKey = "listos"

def putStrLn(str: String): IO[Unit] = IO(println(str))

commandsApi
  .use { redis => // ListCommands[IO, String, String]
    for {
      _ <- redis.rPush(testKey, "one", "two", "three")
      x <- redis.lRange(testKey, 0, 10)
      _ <- putStrLn(s"Range: $x")
      y <- redis.lLen(testKey)
      _ <- putStrLn(s"Length: $y")
      a <- redis.lPop(testKey)
      _ <- putStrLn(s"Left Pop: $a")
      b <- redis.rPop(testKey)
      _ <- putStrLn(s"Right Pop: $b")
      z <- redis.lRange(testKey, 0, 10)
      _ <- putStrLn(s"Range: $z")
    } yield ()
  }
  .unsafeRunSync()
