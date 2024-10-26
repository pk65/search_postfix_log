package com.github.pk65.tools

import scala.io.Source
import cats.effect.{Resource,Async}
import cats.implicits.*
import org.typelevel.log4cats.Logger
import java.io.PrintWriter
import cats.effect.ExitCode
import com.github.pk65.tools.LinesProcessor.LinesStorage
import java.util.zip.GZIPInputStream
import java.io.FileInputStream

object MailLog {

  def closeFile[F[_]: Async : Logger](source: Source): F[Unit] =
    Logger[F].debug("closing source") >>
    source.close().pure[F]

  def makeResourceForRead[F[_]: Async : Logger](inp: Source): Resource[F, Source] =
    Resource.make(inp.pure[F])(src => closeFile(src))

  def getSourceStream(input: String): Source =
    if input.endsWith(".gz") then
      Source.fromInputStream(new GZIPInputStream(new FileInputStream(input)))
    else
      Source.fromFile(input)

  def readWithResource[F[_]: Async : Logger](args: Map[String, String]): F[ExitCode] =
    val argInput = args.get("input")
    val inp: Source = argInput match {
      case Some(input) =>
        getSourceStream(input)
      case None => Source.stdin
    }
    args.get("email") match {
      case Some(email) => Logger[F].debug("making resource from " + argInput.getOrElse("<STDIN>")) >>
        (
          for
            src <- makeResourceForRead(inp)
            dst <- TargetResource.makeResourceForWrite(args.get("output"))
          yield (src, dst)
        ).use((src, dst) =>
          for
            _ <- run(readLinesTrampoline(src.getLines(), LinesStorage(email), dst))
          yield ExitCode.Success
        )
      case None => Logger[F].error("email is required") >>
        Async[F].pure(ExitCode.Error)
    }

  sealed trait TailRec[A] {
    def map[B](f: A => B): TailRec[B] = flatMap(f andThen (Return(_)))
    def flatMap[B](f: A => TailRec[B]): TailRec[B] = FlatMap(this, f)
  }

  final case class Return[A](a: A) extends TailRec[A]
  final case class Suspend[A](resume: () => TailRec[A]) extends TailRec[A]
  final case class FlatMap[A, B](sub: TailRec[A], k: A => TailRec[B]) extends TailRec[B]

  private def run[A](tr: TailRec[A]): A = tr match {
    case Return(a) => a
    case Suspend(r) => run(r())
    case FlatMap(x, f) => x match {
      case Return(a) => run(f(a))
      case Suspend(r) => run(FlatMap(r(), f))
      case FlatMap(y, g) => run(y.flatMap(g(_) `flatMap` f))
    }
  }

  private def readLinesTrampoline[F[_]: Async : Logger](src: Iterator[String], storage: LinesStorage, dst: Option[PrintWriter]): TailRec[F[LinesStorage]] ={
    val line = src.nextOption()
    TargetResource.write(dst, storage.newLine)
    line match {
      case None => Return(storage.pure[F])
      case Some(l) => FlatMap(Suspend(() => readLinesTrampoline(src, LinesProcessor.filterLines(storage.copy(newLine = Some(l))), dst)),
          x => Return(x))
    }
  }
}
