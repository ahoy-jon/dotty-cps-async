package cps

import scala.quoted._
import scala.util.Try
import scala.concurrent.duration._

trait CpsMonad[F[_]] {

   def pure[T](t:T):F[T]

   def map[A,B](fa:F[A])(f: A=>B):F[B]

   def flatMap[A,B](fa:F[A])(f: A=>F[B]):F[B]

}


trait CpsTryMonad[F[_]] extends CpsMonad[F] {

   def error[A](e: Throwable): F[A]

   def restore[A](fa: F[A])(fx:Throwable => F[A]): F[A]

   def withAction[A](fa:F[A])(action: =>Unit):F[A] =
      flatMap(fa){x => 
        try{
          action
          pure(x)
        }catch{
          case ex: Throwable => error(ex)
        }
      }

}


trait CpsAsyncMonad[F[_]] extends CpsTryMonad[F] {

   /**
    * return a future, which will be completed after callback will-be
    * called by the source.
    **/
   def adoptCallbackStyle[A](source: (Try[A]=>Unit) => Unit):F[A]

   def spawn[A](op: =>F[A]): F[A]

   def fulfill[T](t:F[T], timeout: Duration): Option[Try[T]]

}
