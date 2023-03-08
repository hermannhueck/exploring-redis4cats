package l4c

import cats.effect._
import cats.Monad
import cats.syntax.all._
import org.typelevel.log4cats._
// assumes dependency on log4cats-slf4j module
import org.typelevel.log4cats.slf4j.Slf4jFactory

object UsingCapabilities extends IOApp.Simple {

  // create our LoggerFactory
  implicit val logging: LoggerFactory[IO] =
    Slf4jFactory[IO]

  // we summon LoggerFactory instance, and create logger
  val logger: SelfAwareStructuredLogger[IO] =
    LoggerFactory[IO].getLogger

  logger.info("logging in IO!"): IO[Unit]

  // basic example of a service using LoggerFactory
  class LoggerUsingService[F[_]: LoggerFactory: Monad] {
    val logger                     = LoggerFactory[F].getLogger
    def use(args: String): F[Unit] =
      for {
        _ <- logger.info("yay! effect polymorphic code")
        _ <- logger.debug(s"and $args")
      } yield ()
  }

  val service = new LoggerUsingService[IO]

  import util._

  val run: IO[Unit] = for {
    _ <- IO.println(line80.green)
    _ <- service.use("foo")
    _ <- IO.println(line80.green)
  } yield ()
}
