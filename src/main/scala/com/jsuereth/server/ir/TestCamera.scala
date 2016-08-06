package com.jsuereth.server.ir

import java.io.{FileWriter, PrintWriter}

import com.pi4j.io.i2c.{I2CBus, I2CFactory}

object TestCamera {
  def main(args: Array[String]): Unit = {
    val bus = I2CFactory.getInstance(I2CBus.BUS_1)
    val cam = new TrackingCamera(bus)
    // TODO - why have this separate?  Maybe make this know what it's doing.
    cam.init()
    val out = new PrintWriter(new FileWriter("pos.csv"))
    def print(in: String): Unit = { out.print(in); System.out.print(in) }
    try {
      while(true) {
        val now = System.currentTimeMillis()
        val data = cam.readPosition()
        val lasted = System.currentTimeMillis() - now
        val toPrint = for {
          (pos, idx) <- data.zipWithIndex
          if !pos.isEmpty
        } yield s"$now, $lasted, ${idx}, ${pos}\n"
        if (toPrint.isEmpty) print(s"No update for $now\n")
        else System.err.print(toPrint.mkString("\n"))
        Thread.sleep(500)
      }
    } finally out.close()
  }
}
