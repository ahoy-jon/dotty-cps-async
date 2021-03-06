package cps

import org.junit.{Test,Ignore}
import org.junit.Assert._

import scala.quoted._
import scala.util.Success

class TestBS1:

  @Test def tConstantMeta(): Unit = 
     val c = Async.transform[ComputationBound,Int](3)
     assert(c == Done(3))
  
  @Test def tConstantMetaTypeInference(): Unit = 
     val c = async[ComputationBound](3)
     assert(c == Done(3))

  @Test def tAwaitErase(): Unit = 
     val c = Async.async[ComputationBound](await(T1.cb()))
     assert(c == Done(()))
  
  @Test def tValDef(): Unit = 
     val c = Async.async[ComputationBound]{
              val t = 3
             }
     assert(c == Done(()))

  @Test def tValDefAsyn(): Unit = 
     val c = Async.async[ComputationBound]{
         val t = await(T1.cbi(3))
     }
     val c1 = c.run()
     assert( c1 == Success(()) )

  @Test def tBlockNoAsync(): Unit = 
     val c = Async.async[ComputationBound]{
         val x1 = 3
         val x2 = 4 //await(T1.cbi(4))
         x1 + x2
         //7
     }
     val c1 = c.run()
     assert( c1 == Success(7) )

  @Test def tBlockVal2Async(): Unit = 
     val c = Async.async[ComputationBound]{
         val x1 = 3
         val x2 = await(T1.cbi(5))
         x1 + x2
     }
     val c1 = c.run()
     assert( c1 == Success(8) )


