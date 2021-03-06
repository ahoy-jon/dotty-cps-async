package cps.forest

import scala.quoted._

import cps._
 
object IfTransform:

  /**
   *'''
   * '{ if ($cond)  $ifTrue  else $ifFalse } 
   *'''
   **/
  def run[F[_]:Type,T:Type](cpsCtx: TransformationContext[F,T], 
                               cond: Expr[Boolean], ifTrue: Expr[T], ifFalse: Expr[T]
                               )(using qctx: QuoteContext): CpsExpr[F,T] =
     import qctx.tasty.{_, given _}
     import util._
     import cpsCtx._
     val cR = Async.nestTransform(cond, cpsCtx, "C")
     val tR = Async.nestTransform(ifTrue, cpsCtx, "T")
     val fR = Async.nestTransform(ifFalse, cpsCtx, "F")
     var isAsync = true

     val cnBuild = {
       if (!cR.isAsync)
         if (!tR.isAsync && !fR.isAsync) 
            isAsync = false
            CpsExpr.sync(monad, patternCode)
         else
            CpsExpr.async[F,T](monad,
                '{ if ($cond) 
                     ${tR.transformed}
                   else 
                     ${fR.transformed} })
       else // (cR.isAsync) 
         def condAsyncExpr() = cR.transformed
         if (!tR.isAsync && !fR.isAsync) 
           CpsExpr.async[F,T](monad,
                    '{ ${monad}.map(
                                 ${condAsyncExpr()}
                        )( c =>
                                   if (c) {
                                     ${ifTrue}
                                   } else {
                                     ${ifFalse}
                                   } 
                     )})
         else
           CpsExpr.async[F,T](monad,
                   '{ ${monad}.flatMap(
                         ${condAsyncExpr()}
                       )( c =>
                           if (c) {
                              ${tR.transformed}
                           } else {
                              ${fR.transformed} 
                           } 
                        )
                    }) 
       }
     cnBuild
     

