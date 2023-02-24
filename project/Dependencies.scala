import sbt._

object Dependencies {

  import Versions._

  lazy val log4catsCore       = "org.typelevel"  %% "log4cats-core"       % log4catsVersion
  lazy val log4catsSlf4j      = "org.typelevel"  %% "log4cats-slf4j"      % log4catsVersion
  lazy val redis4catsEffects  = "dev.profunktor" %% "redis4cats-effects"  % redis4catsVersion
  lazy val redis4catsStreams  = "dev.profunktor" %% "redis4cats-streams"  % redis4catsVersion
  lazy val redis4catsLog4cats = "dev.profunktor" %% "redis4cats-log4cats" % redis4catsVersion
  lazy val circeCore          = "io.circe"       %% "circe-core"          % circeVersion
  lazy val circeParser        = "io.circe"       %% "circe-parser"        % circeVersion
  lazy val circeGeneric       = "io.circe"       %% "circe-generic"       % circeVersion
  lazy val slf4jApi           = "org.slf4j"       % "slf4j-api"           % slf4jVersion
  lazy val slf4jSimple        = "org.slf4j"       % "slf4j-simple"        % slf4jVersion
  lazy val munit              = "org.scalameta"  %% "munit"               % munitVersion
  // lazy val newtype     = "io.estatico"                   %% "newtype"        % newTypeVersion

  // https://github.com/typelevel/kind-projector
  lazy val kindProjectorPlugin    = compilerPlugin(
    compilerPlugin("org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.full)
  )
  // https://github.com/oleg-py/better-monadic-for
  lazy val betterMonadicForPlugin = compilerPlugin(
    compilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicForVersion)
  )

  val compilerDependencies = Seq(
    log4catsCore,
    log4catsSlf4j,
    redis4catsEffects,
    redis4catsStreams,
    redis4catsLog4cats,
    circeCore,
    circeParser,
    circeGeneric,
    // newtype,
    slf4jApi,
    slf4jSimple,
    munit
  )

  val testDependencies = Seq.empty

  val pluginDependencies = Seq(kindProjectorPlugin, betterMonadicForPlugin)

  val allDependencies = compilerDependencies ++ testDependencies ++ pluginDependencies
}
