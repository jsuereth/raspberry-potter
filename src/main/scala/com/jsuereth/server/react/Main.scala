package com.jsuereth.server.react

import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

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

  val historyImage = history map renderHistory

  /** Draws the current history of tracked objects to an image by connecting points with lines. */
  def renderHistory(buf: CircularBuffer[CameraUpdate]): BufferedImage = {
    // TODO - Mutate an existing image rather than creating new memory each time?
    val img = new BufferedImage(1024, 768, BufferedImage.TYPE_BYTE_GRAY)
    val g = img.getGraphics
    g.setColor(Color.BLACK)
    // TODO - Maybe not use straight lines?
    for {
      (prev, next) <- buf.iterator zip (buf.iterator drop 1)
      (pobj, nobj) <- prev.objects zip next.objects
      if !(pobj.isEmpty || nobj.isEmpty)
    } g.drawLine(pobj.x, pobj.y, nobj.x, nobj.y)
    img
  }


  def main(args: Array[String]): Unit = {
    I2CController.camera onEvent { objs =>
      val msg = (for {
        (obj,idx) <- objs.objects.zipWithIndex
        if !obj.isEmpty
      } yield s"$idx, $obj").mkString("\n")
      System.err.print(s"--== Update ==--\n$msg\n")
    }
    // Dump rendered image to a png file we can look at.
    historyImage map { buf =>
      ImageIO.write(buf, "png", new java.io.File("objects.png"))
    }




    // Here we take over the main thread to run the controller right on it.
    I2CController.run()
  }
}
