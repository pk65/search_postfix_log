package com.github.pk65.tools

import scopt.OParser

case class Config(
    email: Option[String] = None,
    input: Option[String] = None,
    output: Option[String] = None,
    help: Boolean = false,
    verbose: Boolean = false,
    debug: Boolean = false)

object CommandLine {
  def parser(args: List[String]): Map[String, String] = {
    val builder = OParser.builder[Config]
    val parser1 = {
      import builder.*
      OParser.sequence(
        programName("scopt"),
        head("scopt", "4.1.0"),
        opt[String]('e', "email")
          .action((x, c) => c.copy(email = Some(x)))
          .text("mandatory user email"),
        opt[String]('i', "input")
          .action((x, c) => c.copy(input = Some(x)))
          .text("input is Postfix log file [.gz] (plain text or gzipped text)"),
        opt[String]('o', "output")
          .action((x, c) => c.copy(output = Some(x)))
          .text("output is filteredd log file"),
        opt[Unit]('h', "help")
          .action((_, c) => c.copy(help = true))
          .text("print help message and exit"),
        opt[Unit]('v', "verbose")
          .action((_, c) => c.copy(verbose = true))
          .text("verbose output"),
        opt[Unit]('d', "debug")
          .action((_, c) => c.copy(debug = true))
          .text("debug output")
      )
    }

    OParser.parse(parser1, args, Config()) match {
      case Some(config) => 
        if config.help then {
          println(OParser.usage(parser1))
          sys.exit(0)
        }
        List(
          config.input.map("input" -> _),
          config.output.map("output" -> _),
          config.email.map("email" -> _)
        ).flatten.toMap
      case _ =>
        Map.empty[String, String]
    }
  }
}
