package com.github.pk65.tools

import cats.effect.{ExitCode,IO,IOApp,Async}
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.*

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    given [IO[_]: Async]: Logger[IO] = Slf4jLogger.getLogger[IO]
    val inArgs = CommandLine.parser(args)
    for
      _ <- Logger[IO].debug(inArgs.toString)
      exitCode <- MailLog.readWithResource[IO](inArgs)
      _ <- Logger[IO].debug("Content read from selected input")
    yield exitCode
}
