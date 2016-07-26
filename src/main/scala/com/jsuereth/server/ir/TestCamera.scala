package com.jsuereth.server.ir

import java.io.{FileWriter, PrintWriter}

import com.pi4j.io.i2c.{I2CBus, I2CFactory}

object TestCamera {
  def main(args: Array[String]): Unit = {
    val bus = I2CFactory.getInstance(I2CBus.BUS_0)
    val cam = new TrackingCamera(bus)
    // TODO - why have this separate?  Maybe make this know what it's doing.
    cam.init()
    val out = new PrintWriter(new FileWriter("pos.csv"))
    try {
      while(true) {
        val now = System.currentTimeMillis()
        val data = cam.readPosition()
        val lasted = System.currentTimeMillis() - now
        for {
          (pos, idx) <- data.zipWithIndex
          if !pos.isEmpty
        } out.print(s"${idx}, ${pos}")
        Thread.sleep(500)
      }
    } finally out.close()
  }
}
