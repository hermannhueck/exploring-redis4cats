// Keys API

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration._

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val commandsApi: Resource[IO, KeyCommands[IO, String]] =
  Redis[IO].utf8("redis://localhost")

implicit val runtime = cats.effect.unsafe.IORuntime.global

val key = "users"

commandsApi
  .use { redis => // KeyCommands[IO, String]
    for {
      _ <- redis.del(key)
      _ <- redis.exists(key)
      // _ <- redis.expire(key, Duration(5, SECONDS))
      _ <- redis.expire(key, 5.seconds)
    } yield ()
  }
  .unsafeRunSync()
