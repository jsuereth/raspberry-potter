package com.jsuereth.server.ir

import java.io.{FileWriter, PrintWriter}

import com.pi4j.io.i2c.{I2CBus, I2CFactory}
import com.jsuereth.ansi.Ansi

object TestCamera {
  val HEADER = s"${Ansi.CLEAR_SCREEN}${Ansi.MOVE_CURSOR(1,1)}-- Camera Output --"
  def main(args: Array[String]): Unit = {
    val bus = I2CFactory.getInstance(I2CBus.BUS_1)
    val cam = new TrackingCamera(bus)
    // TODO - why have this separate?  Maybe make this know what it's doing.
    cam.init()
    def print(in: String): Unit = { System.out.print(in); }
      while(true) {
        val now = System.currentTimeMillis()
        val data = cam.readPosition()
        val lasted = System.currentTimeMillis() - now
        val toPrint = for {
          (pos, idx) <- data.zipWithIndex
          if !pos.isEmpty
        } yield s"${Ansi.MOVE_CURSOR(idx+3, 1)}${idx}. ${pos} for $lasted time              "
        if (!toPrint.isEmpty) print(s"$HEADER${toPrint.mkString("")}")
        else print(HEADER)
      }
  }
}
