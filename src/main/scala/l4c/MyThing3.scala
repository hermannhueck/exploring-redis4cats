package l4c

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect._
import cats.implicits._

object MyThing3 extends IOApp.Simple {

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  def passForEasierUse[F[_]: Sync: Logger] = for {
    _         <- Logger[F].info("Logging at start of passForEasierUse")
    something <- Sync[F]
                   .delay(println("I could do anything"))
                   .onError { case e => Logger[F].error(e)("Something Went Wrong in passForEasierUse") }
    _         <- Logger[F].info("Logging at end of passForEasierUse")
  } yield something

  import util._

  val run: IO[Unit] = for {
    _ <- IO.println(line80.green)
    _ <- passForEasierUse[IO]
    _ <- IO.println(line80.green)
  } yield ()
}
