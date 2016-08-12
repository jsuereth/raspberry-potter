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

  /** Defines a new signal that returns the tracked object poisition for the various objects. */
  def trackedObjects: Signal[Seq[TrackedObjectUpdate]] = {
    // TODO - Once a position "dies" we should kill the object, also ignore updates where all positions are empty,
    // not updates where we have some positions....
    def empty = Seq.fill(4)(TrackedObjectUpdate.empty)
    cameraE.scanPast(empty) { (lastPost, event) =>
      for {
        (last, next) <- lastPost zip event.objects
      } yield if (next.isEmpty) last else next
    }.toSignal(empty)
  }

  /** This is the thread which will read updates from the camera. */
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
      // TODO - how long should we delay here?  Should we delay at all?
    }
  } catch {
    case t: Throwable => cameraE except t
  }

  def startAsThread(): Unit = {
    val i2cthread = new Thread(I2CController, "i2c-controller")
    i2cthread.setDaemon(true)
    i2cthread.start()
  }
}


/** An update from the camera. */
case class CameraUpdate(objects: Seq[TrackedObjectUpdate], timestamp: Long)
