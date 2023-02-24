// Strings API

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val commandsApi: Resource[IO, StringCommands[IO, String, String]] =
  Redis[IO].utf8("redis://localhost")

implicit val runtime = cats.effect.unsafe.IORuntime.global

val usernameKey = "users"

def putStrLn(str: String): IO[Unit] = IO(println(str))

val showResult: Option[String] => IO[Unit] =
  _.fold(putStrLn(s"Not found key: $usernameKey"))(s => putStrLn(s))

commandsApi
  .use { redis => // StringCommands[IO, String, String]
    for {
      x <- redis.get(usernameKey)
      _ <- showResult(x)
      _ <- redis.set(usernameKey, "gvolpe")
      y <- redis.get(usernameKey)
      _ <- showResult(y)
      _ <- redis.setNx(usernameKey, "should not happen")
      w <- redis.get(usernameKey)
      _ <- showResult(w)
    } yield ()
  }
  .unsafeRunSync()
