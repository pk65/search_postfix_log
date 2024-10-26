import munit.FunSuite
import scala.io.Source
import com.github.pk65.tools.LinesProcessor as LP
import scala.annotation.tailrec

class LinesProcessorSuite extends FunSuite {
  val expectedText = """Oct 03 09:11:20 service9 postfix/cleanup[1221726]: 54BE7192007D: message-id=<657720787.4.1727939480336.JavaMail.tomcat7@service9>
    |Oct 03 09:11:20 service9 postfix/cleanup[1221726]: 54BE7192007D: warning: header Subject: [Service 9] Report acceptance request for period 2024-10 from localhost[127.0.0.1]; from=<service9@example.com> to=<USER1@EXAMPLE.COM> proto=ESMTP helo=<service9>
    |Oct 03 09:11:20 service9 postfix/qmgr[1065995]: 54BE7192007D: from=<service9@example.com>, size=777, nrcpt=1 (queue active)
    |Oct 03 09:11:20 service9 postfix/smtp[1221727]: 54BE7192007D: to=<USER1@EXAMPLE.COM>, relay=mailrelay.example.com[135.239.3.83]:25, delay=0.37, delays=0.01/0.01/0.21/0.14, dsn=2.0.0, status=sent (250 2.0.0 4937BKB9001631 Message accepted for delivery)
    |Oct 03 09:11:20 service9 postfix/smtpd[1221723]: 54BE7192007D: client=localhost[127.0.0.1]
    |Oct 03 09:11:20 service9 postfix/qmgr[1065995]: 54BE7192007D: removed""".stripMargin.split("\n").nn.mkString(LP.LINE_SEPARATOR)
  val resourcePath = getClass.getResource("/mail.log").nn.getPath.nn


  test("add lines one by one to the storage") {
    val stream = Source.fromFile(resourcePath)
    assertEquals(readLines(stream.getLines(), LP.LinesStorage("user1@example.com")).mkString(LP.LINE_SEPARATOR), expectedText)
  }

  @tailrec
  private def readLines(src: Iterator[String], storage: LP.LinesStorage, filteredLines: List[String] = List.empty): List[String] = {
    val line = src.nextOption()
    val lines = storage.newLine match {
      case None => filteredLines
      case Some(l) => filteredLines :+ l
    }
    line match {
      case None => lines
      case Some(l) => readLines(src, LP.filterLines(storage.copy(newLine = Some(l))), lines)
    }
  }
}