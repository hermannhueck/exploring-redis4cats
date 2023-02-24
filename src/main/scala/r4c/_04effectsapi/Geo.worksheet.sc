// Geo API

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra._
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import dev.profunktor.redis4cats.effects._
import io.lettuce.core.GeoArgs

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val commandsApi: Resource[IO, GeoCommands[IO, String, String]] =
  Redis[IO].utf8("redis://localhost")

implicit val runtime = cats.effect.unsafe.IORuntime.global

val testKey = "location"

def putStrLn(str: String): IO[Unit] = IO(println(str))

val _BuenosAires  = GeoLocation(Longitude(-58.3816), Latitude(-34.6037), "Buenos Aires")
val _RioDeJaneiro = GeoLocation(Longitude(-43.1729), Latitude(-22.9068), "Rio de Janeiro")
val _Montevideo   = GeoLocation(Longitude(-56.164532), Latitude(-34.901112), "Montevideo")
val _Tokyo        = GeoLocation(Longitude(139.6917), Latitude(35.6895), "Tokyo")

commandsApi
  .use { redis => // GeoCommands[IO, String, String]
    for {
      _ <- redis.geoAdd(testKey, _BuenosAires)
      _ <- redis.geoAdd(testKey, _RioDeJaneiro)
      _ <- redis.geoAdd(testKey, _Montevideo)
      _ <- redis.geoAdd(testKey, _Tokyo)
      x <- redis.geoDist(testKey, _BuenosAires.value, _Tokyo.value, GeoArgs.Unit.km)
      _ <- putStrLn(s"Distance from ${_BuenosAires.value} to Tokyo: $x km")
      y <- redis.geoPos(testKey, _RioDeJaneiro.value)
      _ <- putStrLn(s"Geo Pos of ${_RioDeJaneiro.value}: ${y.headOption}")
      z <- redis.geoRadius(testKey, GeoRadius(_Montevideo.lon, _Montevideo.lat, Distance(10000.0)), GeoArgs.Unit.km)
      _ <- putStrLn(s"Geo Radius in 1000 km: $z")
    } yield ()
  }
  .unsafeRunSync()
