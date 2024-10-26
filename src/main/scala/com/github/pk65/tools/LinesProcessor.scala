package com.github.pk65.tools

import scala.util.matching.Regex.Match
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object LinesProcessor {
  val LINE_SEPARATOR: String = System.lineSeparator().nn

  case class LinesStorage(personsEmail: String = "",
   newLine: Option[String] = None,
   reqListOfEmail: Set[(String, String, String)] = Set.empty[(String, String, String)],
   buffer: Map[String, List[String]] = Map.empty[String, List[String]])

  def filterLines(storage: LinesStorage): LinesStorage = {
    storage.newLine match {
      case Some(line) => processNewLine(storage, line)
      case None => storage
    }
  }

  def regexMatchAsMap(mt: Option[Match], keyNames: List[String]): Map[String, String] = {
    mt.map(m => keyNames.zip(keyNames.map(k => 
      Try(m.group(k).nn) match {
        case Success(s) => Some(s)
        case Failure(_) => None
      }
    )).filter(_._2.isDefined).map(p => (p._1, p._2.get)).toMap)
      .getOrElse(Map.empty)
  }

  private def processNewLine(storage: LinesStorage, newLine: String): LinesStorage = {
    val reqidPattern = """^\w+ \d+ \d{2}:\d{2}:\d{2} \S+ \S+\[\d+\]: (?<reqid>[\dA-F]+):(.*( (?<action>to)=<(?<email>[^>]+)>)| (?<method>removed))?.*""".r
    val keyNames = List("email", "reqid", "action", "method")

    val emailItem = reqidPattern.findAllMatchIn(newLine)
      .map(m => regexMatchAsMap(Some(m), keyNames))
      .filter(x => x.get("email").exists(e => e.equalsIgnoreCase(storage.personsEmail)))
      .map(x => (x.get("email").get, x.get("reqid").get, x.get("action").get))
      .toSet
    val reqListOfEmail = storage.reqListOfEmail ++ emailItem
    val reqListOfMethod: Set[String] = 
        reqidPattern.findAllMatchIn(newLine)
          .map(m => regexMatchAsMap(Some(m), keyNames))
          .filter(x => x.get("method").exists(e => e.equalsIgnoreCase("removed")))
          .map(x => x.get("reqid").get)
          .toSet
    val openEmailReqIdList = reqListOfEmail.filter(x => !reqListOfMethod.contains(x._2))
    val currentLineParsed: Map[String, String] = regexMatchAsMap(reqidPattern.findFirstMatchIn(newLine), keyNames)
    val newBuffer = currentLineParsed.get("reqid").map(reqId => 
      storage.buffer.get(reqId) match {
        case Some(list) => if reqListOfEmail.exists(y => y._2 == reqId) then
                           storage.buffer - reqId
                         else
                           storage.buffer + (reqId -> (list :+ newLine))
        case None => storage.buffer + (reqId -> List(newLine))
      }
    ).getOrElse(storage.buffer)
    val newLineIsOk = currentLineParsed.get("reqid").exists(reqId => 
      (reqListOfEmail.exists(y => y._2 == reqId && y._1.equalsIgnoreCase(storage.personsEmail)))
    )
    val newLineWithBufferPrepended = currentLineParsed.get("reqid").map(reqId => storage.buffer.get(reqId) match {
      case Some(list) => if storage.reqListOfEmail.exists(y => y._2 == reqId) then newLine else list.mkString(LINE_SEPARATOR) + LINE_SEPARATOR + newLine
      case None => newLine
    }).getOrElse(newLine)
    storage.copy(reqListOfEmail = openEmailReqIdList, newLine = if newLineIsOk then Some(newLineWithBufferPrepended) else None, buffer = newBuffer)
  }
}
