package l4c

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect._
import cats.implicits._

object MyThing extends IOApp.Simple {
  // Impure But What 90% of Folks I know do with log4s
  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  // Arbitrary Local Function Declaration
  def doSomething[F[_]: Sync]: F[Unit] =
    Logger[F].info("Logging Start Something") *>
      Sync[F]
        .delay(println("I could be doing anything"))
        .attempt
        .flatMap {
          case Left(e)  => Logger[F].error(e)("Something Went Wrong")
          case Right(_) => Sync[F].pure(())
        }

  import util._

  val run: IO[Unit] = for {
    _ <- IO.println(line80.green)
    _ <- doSomething[IO]
    _ <- IO.println(line80.green)
  } yield ()
}
