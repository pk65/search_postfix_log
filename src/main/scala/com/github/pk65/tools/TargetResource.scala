package com.github.pk65.tools

import cats.effect.Async
import java.io.FileOutputStream
import cats.effect.Resource
import java.io.PrintWriter
import java.nio.charset.Charset

object TargetResource {
  def open[F[_]: Async](path: Option[String]): F[Option[PrintWriter]] =
   Async[F].delay(
    path match {
      case None => None
      case Some(p) => Some(new PrintWriter(new FileOutputStream(p), true, Charset.forName("UTF-8")))
    }
   )
  
  def close[F[_]: Async](file: Option[PrintWriter]): F[Unit] =
    file match {
      case None => Async[F].unit
      case Some(f) => Async[F].delay(f.close())
    }

  def makeResourceForWrite[F[_]: Async](path: Option[String]): Resource[F, Option[PrintWriter]] =
    Resource.make(open(path))(f => close(f))

  def write(dst: Option[PrintWriter], content: Option[String]): Unit =
    content match {
      case None => ()
      case Some(c) => dst match {
        case Some(d) => d.println(c)
        case None => println(c)
      }
    }
}
