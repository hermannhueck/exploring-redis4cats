package l4c

import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect._
import cats.implicits._

object MyThing2 extends IOApp.Simple {

  def safelyDoThings[F[_]: Sync]: F[Unit] = for {
    logger    <- Slf4jLogger.create[F]
    _         <- logger.info("Logging at start of safelyDoThings")
    something <- Sync[F]
                   .delay(println("I could do anything"))
                   .onError { case e => logger.error(e)("Something Went Wrong in safelyDoThings") }
    _         <- logger.info("Logging at end of safelyDoThings")
  } yield something

  import util._

  val run: IO[Unit] = for {
    _ <- IO.println(line80.green)
    _ <- safelyDoThings[IO]
    _ <- IO.println(line80.green)
  } yield ()
}
