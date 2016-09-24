package com.jsuereth.server.react

import com.jsuereth.server.ir.TrackedObjectUpdate
import com.pi4j.io.i2c.{I2CBus, I2CFactory}
import io.reactors.{Events, Signal}

/**
  * Event streams from the I2C bus.
  *
  * This class is effectively a controller for i2c.
  */
object I2CController extends Runnable {
  private val cameraE = new Events.Emitter[CameraUpdate]
  /** A stream of camera update events. */
  def camera: Events[CameraUpdate] = cameraE

  /** This is the thread which will read updates from the camera as often as it can. */
  final def run(): Unit = try {
    val bus = I2CFactory.getInstance(I2CBus.BUS_1)
    val c = new com.jsuereth.server.ir.TrackingCamera(bus)
     c.init()
    while (true) {
      val data = c.readPosition()
      if (!data.forall(_.isEmpty)) {
        val update = CameraUpdate(data, System.currentTimeMillis)
        cameraE react update
      }
      // TODO - what refresh rate do we want here?
    }
  } catch {
    case t: Throwable =>
      // Also, on exception should we try to re-initialize the camera?
      cameraE except t
  }

  def startAsThread(): Unit = {
    val i2cthread = new Thread(I2CController, "i2c-controller")
    i2cthread.setDaemon(true)
    i2cthread.start()
  }
}


/** An update from the camera. */
case class CameraUpdate(objects: Seq[TrackedObjectUpdate], timestamp: Long)
