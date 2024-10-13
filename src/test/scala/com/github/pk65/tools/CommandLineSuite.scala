package com.github.pk65.tools

import munit.CatsEffectSuite

class CommandLineSuite extends CatsEffectSuite {

  test("both input and output files are provided") {
    val args = "-i" :: "mail.log" :: "-o" :: "output.log" :: "-e" :: "user@example.com" :: Nil
    assertEquals(CommandLine.parser(args), Map("input" -> "mail.log", "output" -> "output.log", "email" -> "user@example.com"))
  }
  test("only input file and email is provided") {
    val args = "-i" :: "mail.log" :: "-e" :: "user@example.com" :: Nil
    assertEquals(CommandLine.parser(args), Map("input" -> "mail.log", "email" -> "user@example.com"))
  }

  test("only email is provided") {
    val args = "-e" :: "user@example.com" :: Nil
    assertEquals(CommandLine.parser(args), Map("email" -> "user@example.com"))
  }
}