package com.github.pk65.tools

import com.github.pk65.tools.MailLog.LinesStorage

object LinesProcessor {
  def filterLines(storage: LinesStorage): LinesStorage = {
    storage.newLine match {
      case Some(line) => processNewLine(storage, line)
      case None => storage
    }
  }

  private def processNewLine(storage: LinesStorage, newLine: String): LinesStorage = {
    val reqidPattern = """^\w+ \d+ \d{2}:\d{2}:\d{2} \w+ \w+\/\w+\[\d+\]: (?<reqid>[\dA-F]+):(.*( (?<action>to)=<(?<email>[^>]+)>)| (?<method>removed))?.*""".r
    val keyNames = List("email", "reqid", "action")
    val emailItem = reqidPattern.findAllMatchIn(newLine)
      .map(m => (m.group("email"), m.group("reqid"), m.group("action"))).filter(x => x._1 != null && x._1.equalsIgnoreCase(storage.personsEmail)).toSet
    val reqListOfEmail = storage.reqListOfEmail ++ emailItem
    val reqListOfMethod: Set[String] = 
        reqidPattern.findAllMatchIn(newLine)
          .map(m => (m.group("method"), m.group("reqid"))).toList.filter(x => x._1 == "removed") match {
            case Nil => Set.empty
            case x => x.map(_._2).toSet
          }
    val openEmailReqIdList = reqListOfEmail.filter(x => !reqListOfMethod.contains(x._2))
    val currentLineParsed: Map[String, String] = reqidPattern.findFirstMatchIn(newLine)
      .map(m => keyNames.zip(keyNames.map(k => m.group(k)).toList).filterNot(_._2 == null).toMap)
      .getOrElse(Map.empty)
    val newLineIsOk = currentLineParsed.get("reqid").exists(reqId => 
      (reqListOfEmail.exists(y => y._2 == reqId && y._1.equalsIgnoreCase(storage.personsEmail)))
    )
    storage.copy(reqListOfEmail = openEmailReqIdList, newLine = if (newLineIsOk) Some(newLine) else None)
  }
}
