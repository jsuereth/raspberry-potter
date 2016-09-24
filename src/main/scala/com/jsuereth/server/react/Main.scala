package com.jsuereth.server.react

import com.jsuereth.server.collections.CircularBuffer
import com.jsuereth.server.ir.TrackedObjectUpdate
import io.reactors.Events
/**
  * Runs the main reactionary programming.
  */
object Main {
  val NumPointsToKeep = 60

  val history = {
    val tmp =
      new Events.Mutable[CircularBuffer[CameraUpdate]](
        CircularBuffer.withSize[CameraUpdate](NumPointsToKeep)
      )
    I2CController.camera.mutate(tmp) { buf => up =>
      buf put up
    }
    tmp
  }


  def main(args: Array[String]): Unit = {
    I2CController.camera onEvent { objs =>
      val msg = (for {
        (obj,idx) <- objs.objects.zipWithIndex
        if !obj.isEmpty
      } yield s"$idx, $obj").mkString("\n")
      System.err.print(s"--== Update ==--\n$msg\n")
    }




    // Here we take over the main thread to run the controller right on it.
    I2CController.run()
  }
}
