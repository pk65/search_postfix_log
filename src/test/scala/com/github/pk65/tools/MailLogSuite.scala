package com.github.pk65.tools

import munit.CatsEffectSuite
import cats.effect.{IO, Async}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.io.Source
import com.github.pk65.tools.LinesProcessor as LP

class MailLogSuite extends CatsEffectSuite {
  given [IO[_]: Async]: Logger[IO] = Slf4jLogger.getLogger[IO]
  val expectedText = """Oct 03 09:11:20 service9 postfix/cleanup[1221726]: 54BE7192007D: message-id=<657720787.4.1727939480336.JavaMail.tomcat7@service9>
    |Oct 03 09:11:20 service9 postfix/cleanup[1221726]: 54BE7192007D: warning: header Subject: [Service 9] Report acceptance request for period 2024-10 from localhost[127.0.0.1]; from=<service9@example.com> to=<USER1@EXAMPLE.COM> proto=ESMTP helo=<service9>
    |Oct 03 09:11:20 service9 postfix/qmgr[1065995]: 54BE7192007D: from=<service9@example.com>, size=777, nrcpt=1 (queue active)
    |Oct 03 09:11:20 service9 postfix/smtp[1221727]: 54BE7192007D: to=<USER1@EXAMPLE.COM>, relay=mailrelay.example.com[135.239.3.83]:25, delay=0.37, delays=0.01/0.01/0.21/0.14, dsn=2.0.0, status=sent (250 2.0.0 4937BKB9001631 Message accepted for delivery)
    |Oct 03 09:11:20 service9 postfix/smtpd[1221723]: 54BE7192007D: client=localhost[127.0.0.1]
    |Oct 03 09:11:20 service9 postfix/qmgr[1065995]: 54BE7192007D: removed""".stripMargin.split("\n").mkString(LP.LINE_SEPARATOR)

  test("readWithResource should return filtered content of the file") {
    val resourcePath = getClass.getResource("/mail.log").getPath
    val filteredOutput = s"$resourcePath.filtered"
    val args = CommandLine.parser(List("-i", resourcePath, "-e", "user1@example.com", "-o", filteredOutput))
    for {
      _ <- Logger[IO].debug(args.toString)
      content <- MailLog.readWithResource[IO](args)
      filteredLines <- IO.blocking(Source.fromFile(filteredOutput).getLines().mkString(LP.LINE_SEPARATOR))
    } yield assertEquals(filteredLines, expectedText)
  }
}
