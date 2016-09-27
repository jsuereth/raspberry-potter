package com.jsuereth.server.react

import java.awt.{Color, Graphics}
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

import com.jsuereth.server.collections.CircularBuffer
import com.jsuereth.server.ir.TrackedObjectUpdate
import io.reactors.{Events, ReactorSystem}
import io.reactors.concurrent.Services.Clock

import scala.concurrent.duration.Duration

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
  // An image of the "trails" of data we have. 1024x768 (matches camera).
  val historyImage = {
    // Re-use the same image and re-render every time we get a new camera tick.
    val tmp = new Events.Mutable[BufferedImage](new BufferedImage(1024, 768, BufferedImage.TYPE_BYTE_GRAY))
    history.mutate(tmp) { img => buf =>
      drawHistory(buf, img.getGraphics)
    }
    tmp
  }

  /** Draws the current history of tracked objects to an image by connecting points with lines. */
  def drawHistory(buf: CircularBuffer[CameraUpdate], g: Graphics): Unit = {
    g.setColor(Color.WHITE)
    g.drawRect(0,0,1024,728)
    g.setColor(Color.BLACK)
    // TODO - Maybe not use straight lines?
    for {
      (prev, next) <- buf.iterator zip (buf.iterator drop 1)
      (pobj, nobj) <- prev.objects zip next.objects
      if !(pobj.isEmpty || nobj.isEmpty)
    } g.drawLine(pobj.x, pobj.y, nobj.x, nobj.y)
  }




  def main(args: Array[String]): Unit = {
    val system = ReactorSystem.default("clock")
    val clock = new Clock(system)

    I2CController.camera onEvent { objs =>
      val msg = (for {
        (obj,idx) <- objs.objects.zipWithIndex
        if !obj.isEmpty
      } yield s"$idx, $obj").mkString("\n")
      System.err.print(s"--== Update ==--\n$msg\n")
    }
    // Dump rendered image to a png file we can look at.
    //historyImage map { buf =>
    //  ImageIO.write(buf, "png", new java.io.File("objects.png"))
    //}
    val snapshotClock = clock.periodic(Duration(10, TimeUnit.SECONDS))
    // TODO - drive the hardware using the clock against the ReactorSystem....
    snapshotClock map { count =>
     historyImage.once map { img =>
       ImageIO.write(img, "png", new java.io.File(s"objects_${count}.png"))
     }
    }




    // Here we take over the main thread to run the controller right on it.
    try I2CController.run()
    finally system.shutdown()
  }
}