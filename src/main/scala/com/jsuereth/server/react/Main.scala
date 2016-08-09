package com.jsuereth.server.react

/**
  * Runs the main reactionary programming.
  */
object Main {
  def main(args: Array[String]): Unit = {
    I2CController.trackedObjects onEvent { objs =>
      val msg = (for {
        (obj,idx) <- objs.zipWithIndex
        if !obj.isEmpty
      } yield s"$idx, $obj").mkString("\n")
      System.err.print(s"--== Update ==--\n$msg\n")
    }
    // Here we take over the main thread to run the controller right on it.
    I2CController.run()
  }
}
