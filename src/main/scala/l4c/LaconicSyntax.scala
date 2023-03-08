package l4c

import cats.implicits._
import cats.Applicative
import cats.effect._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.syntax._

object LaconicSyntax extends IOApp.Simple {

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  def successComputation[F[_]: Applicative]: F[Int] =
    Applicative[F].pure(1)
  def errorComputation[F[_]: Sync]: F[Unit]         =
    Sync[F].raiseError[Unit](new Throwable("Sorry!"))

  def log[F[_]: Sync: Logger] =
    for {
      result1 <- successComputation[F]
      _       <- info"First result is $result1"
      _       <- errorComputation[F].onError { case _ =>
                   error"We got an error!"
                 }
    } yield ()

  import util._

  val run: IO[Unit] = for {
    _ <- IO.println(line80.green)
    _ <- log[IO]
    _ <- IO.println(line80.green)
  } yield ()
}
