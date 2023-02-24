// Redis Client
//===============

// ----- Establishing connection

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.algebra.StringCommands
import dev.profunktor.redis4cats.connection._
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val stringCodec: RedisCodec[String, String] = RedisCodec.Utf8

val commandsApi: Resource[IO, StringCommands[IO, String, String]] =
  RedisClient[IO]
    .from("redis://localhost")
    .flatMap(client => Redis[IO].fromClient(client, stringCodec))

// ----- Client configuration

import dev.profunktor.redis4cats.config._
import io.lettuce.core.{ClientOptions, TimeoutOptions}
import java.time.Duration

val mkOpts: IO[ClientOptions] =
  IO {
    ClientOptions
      .builder()
      .autoReconnect(false)
      .pingBeforeActivateConnection(false)
      .timeoutOptions(
        TimeoutOptions
          .builder()
          .fixedTimeout(Duration.ofSeconds(10))
          .build()
      )
      .build()
  }

val api: Resource[IO, StringCommands[IO, String, String]] =
  for {
    opts   <- Resource.eval(mkOpts)
    client <- RedisClient[IO].withOptions("redis://localhost", opts)
    redis  <- Redis[IO].fromClient(client, stringCodec)
  } yield redis

import scala.concurrent.duration._

val config: Redis4CatsConfig =
  Redis4CatsConfig()
    .withShutdown(ShutdownConfig(1.seconds, 5.seconds))

val configuredApi: Resource[IO, StringCommands[IO, String, String]] =
  for {
    uri    <- Resource.eval(RedisURI.make[IO]("redis://localhost"))
    opts   <- Resource.eval(mkOpts)
    client <- RedisClient[IO].custom(uri, opts, config)
    redis  <- Redis[IO].fromClient(client, stringCodec)
  } yield redis

// ----- Single node connection

val simpleApi: Resource[IO, StringCommands[IO, String, String]] =
  Redis[IO].simple("redis://localhost", RedisCodec.Ascii)

val simpleOptsApi: Resource[IO, StringCommands[IO, String, String]] =
  Resource.eval(IO(ClientOptions.create())).flatMap { opts =>
    Redis[IO].withOptions("redis://localhost", opts, RedisCodec.Ascii)
  }

val utf8Api: Resource[IO, StringCommands[IO, String, String]] =
  Redis[IO].utf8("redis://localhost")

// ----- Logger

// Disable logging

// Available for any `Applicative[F]`
// import dev.profunktor.redis4cats.effect.Log.NoOp._

// Enable logging for Stdout

// Available for any `Sync[F]`
// import dev.profunktor.redis4cats.effect.Log.Stdout._

// ----- Standalone, Sentinel or Cluster

// You can connect in any of these modes by either using JRedisURI.create or JRedisURI.Builder.
// More information here: https://github.com/lettuce-io/lettuce-core/wiki/Redis-URI-and-connection-details

// ----- Cluster connection

val clusterApi: Resource[IO, StringCommands[IO, String, String]] =
  for {
    uri    <- Resource.eval(RedisURI.make[IO]("redis://localhost:30001"))
    client <- RedisClusterClient[IO](uri)
    redis  <- Redis[IO].fromClusterClient(client, stringCodec)()
  } yield redis

val clusterUtf8Api: Resource[IO, StringCommands[IO, String, String]] =
  Redis[IO].clusterUtf8("redis://localhost:30001")()

// ----- Master / Replica connection

import io.lettuce.core.{ReadFrom => JReadFrom}

trait XXX[F[_]] {

  def make[K, V](
      codec: RedisCodec[K, V],
      uris: RedisURI*
  )(readFrom: Option[JReadFrom] = None): Resource[F, RedisMasterReplica[K, V]]

  def withOptions[K, V](
      codec: RedisCodec[K, V],
      opts: ClientOptions,
      uris: RedisURI*
  )(readFrom: Option[JReadFrom] = None): Resource[F, RedisMasterReplica[K, V]]
}

// ----- Example using the Strings API

import cats.syntax.option._
import dev.profunktor.redis4cats.data.ReadFrom

val commands: Resource[IO, StringCommands[IO, String, String]] =
  for {
    uri   <- Resource.eval(RedisURI.make[IO]("redis://localhost"))
    conn  <- RedisMasterReplica[IO].make(RedisCodec.Utf8, uri)(ReadFrom.UpstreamPreferred.some)
    redis <- Redis[IO].masterReplica(conn)
  } yield redis

commands.use { redis =>
  redis.set("foo", "123") >> IO.unit // do something
}
