// Bitmaps API

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val commandsApi: Resource[IO, BitCommands[IO, String, String]] =
  Redis[IO].utf8("redis://localhost")

implicit val runtime = cats.effect.unsafe.IORuntime.global

val testKey  = "foo"
val testKey2 = "bar"
val testKey3 = "baz"

def putStrLn(str: String): IO[Unit] = IO(println(str))

commandsApi
  .use { cmd => // BitCommands[IO, String, String]
    for {
      a  <- cmd.setBit(testKey, 7, 1)
      _  <- cmd.setBit(testKey2, 7, 0)
      _  <- putStrLn(s"Set as $a")
      b  <- cmd.getBit(testKey, 6)
      _  <- putStrLn(s"Bit at offset 6 is $b")
      _  <- cmd.bitOpOr(testKey3, testKey, testKey2)
      _  <- for {
              s1 <- cmd.setBit("bitmapsarestrings", 2, 1)
              s2 <- cmd.setBit("bitmapsarestrings", 3, 1)
              s3 <- cmd.setBit("bitmapsarestrings", 5, 1)
              s4 <- cmd.setBit("bitmapsarestrings", 10, 1)
              s5 <- cmd.setBit("bitmapsarestrings", 11, 1)
              s6 <- cmd.setBit("bitmapsarestrings", 14, 1)
            } yield s1 + s2 + s3 + s4 + s5 + s6
      bf <- cmd.bitField(
              "inmap",
              BitCommandOperation.SetUnsigned(2, 1),
              BitCommandOperation.SetUnsigned(3, 1),
              BitCommandOperation.SetUnsigned(5, 1),
              BitCommandOperation.SetUnsigned(10, 1),
              BitCommandOperation.SetUnsigned(11, 1),
              BitCommandOperation.SetUnsigned(14, 1),
              BitCommandOperation.IncrUnsignedBy(14, 1)
            )
      _  <- IO.println(s"Via bitfield $bf")
    } yield ()
  }
  .unsafeRunSync()
