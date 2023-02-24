// Sorted Sets API

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import dev.profunktor.redis4cats.effects.{Score, ScoreWithValue, ZRange}

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val commandsApi: Resource[IO, SortedSetCommands[IO, String, String]] =
  Redis[IO].utf8("redis://localhost")

implicit val runtime = cats.effect.unsafe.IORuntime.global

val testKey = "zztop"

def putStrLn(str: String): IO[Unit] = IO(println(str))

commandsApi
  .use { redis => // SortedSetCommands[IO, String, String]
    for {
      _ <- redis.zAdd(testKey, args = None, ScoreWithValue(Score(1), "1"), ScoreWithValue(Score(3), "2"))
      x <- redis.zRevRangeByScore(testKey, ZRange(0, 2), limit = None)
      _ <- putStrLn(s"Score: $x")
      y <- redis.zCard(testKey)
      _ <- putStrLn(s"Size: $y")
      z <- redis.zCount(testKey, ZRange(0, 1))
      _ <- putStrLn(s"Count: $z")
    } yield ()
  }
  .unsafeRunSync()
