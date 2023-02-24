// Scripting API

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val commandsApi: Resource[IO, ScriptCommands[IO, String, String]] =
  Redis[IO].utf8("redis://localhost")

implicit val runtime = cats.effect.unsafe.IORuntime.global

def putStrLn(str: String): IO[Unit] = IO(println(str))

commandsApi
  .use { redis => // ScriptCommands[IO, String, String]
    for {
      // returns a String according the value codec (the last type parameter of ScriptCommands)
      greeting <- redis.eval("return 'Hello World'", effects.ScriptOutputType.Value)
      _        <- putStrLn(s"Greetings from Lua: $greeting")
    } yield ()
  }
  .unsafeRunSync()
