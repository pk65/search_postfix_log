package com.github.pk65.tools

import scopt.OParser

case class Config(
    email: Option[String] = None,
    input: Option[String] = None,
    output: Option[String] = None,
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
          .text("input is Postfix log file"),
        opt[String]('o', "output")
          .action((x, c) => c.copy(output = Some(x)))
          .text("output is filteredd log file"),
      )
    }

    OParser.parse(parser1, args, Config()) match {
      case Some(config) => 
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
