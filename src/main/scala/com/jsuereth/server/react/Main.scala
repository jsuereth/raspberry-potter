package com.jsuereth.server.react

/**
  * Runs the main reactionary programming.
  */
object Main {
  def main(args: Array[String]): Unit = {
    I2CController.start
    I2CController.trackedObjects onEvent { objs =>
      val msg = (for {
        (obj,idx) <- objs.zipWithIndex
      } yield s"$idx, $obj").mkString("\n")
      System.err.print(s"--== Update ==--\n$msg\n")
    }
    // TODO - wait until done?
    while (true) {
      Thread.sleep(1000L)
    }
  }
}
