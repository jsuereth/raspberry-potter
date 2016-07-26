package com.jsuereth.server.ir

import com.pi4j.io.i2c.I2CBus



/**
  * Controller for the DFRobot Camera.  Lifts raw byte interface into something more manageable.
  *
  *
  * Note: byte buffer layout: http://wiibrew.org/wiki/Wiimote#IR_Camera
  */
class TrackingCamera(bus: I2CBus) {
  import TrackingCamera._
  private val sendCamera = bus.getDevice(SendDeviceId)
  private val readCamera = bus.getDevice(ReadDeviceId)
  private val buf = new Array[Byte](36)

  /** Initializes the camera hardware. */
  def init(): Unit = {
    sendCamera.write(0x30.toByte)
    sendCamera.write(0x01.toByte)
    Thread.sleep(10);
    sendCamera.write(0x30.toByte)
    sendCamera.write(0x08.toByte)
    Thread.sleep(10);
    sendCamera.write(0x06.toByte)
    sendCamera.write(0x90.toByte)
    Thread.sleep(10);
    sendCamera.write(0x08.toByte)
    sendCamera.write(0xC0.toByte)
    Thread.sleep(10);
    sendCamera.write(0x1A.toByte)
    sendCamera.write(0x40.toByte)
    Thread.sleep(10);
    sendCamera.write(0x33.toByte)
    sendCamera.write(0x33.toByte)
    /*
     // IR sensor initialize
    Write_2bytes(0x30,0x01); delay(10);
    Write_2bytes(0x30,0x08); delay(10);
    Write_2bytes(0x06,0x90); delay(10);
    Write_2bytes(0x08,0xC0); delay(10);
    Write_2bytes(0x1A,0x40); delay(10);
    Write_2bytes(0x33,0x33); delay(10);
     */
  }

  def readPosition(): Seq[TrackedObjectUpdate] = {
    // TODO - example code shows reading 16 bytes but w/ extended object positions.....
    //readPositionsExtended()
    readPositionsAdruinoExample()
  }

  private def clearBuf(): Unit = java.util.Arrays.fill(buf, 0, 16, 0.toByte)


  private def readPositionsAdruinoExample(): Seq[ExtendedTrackedObjectUpdate] = {
    clearBuf()
    readCamera.read(0, buf, 0, 16) match {
      case 16 =>
        for(i <- 0 until 4) yield readExtendedObjectFromBuf(1 + (i*3))
      case n =>
        throw new CameraException(s"Failed to read basic positions from camera, only found $n bytes available")
    }
  }


  /** Reads camera positions when the camera is in basic reporting mode. */
  private def readPositionsBasic(): Seq[BasicTrackedObjectUpdate] = {
    clearBuf()
    readCamera.read(0, buf, 0, 10) match {
      case 10 =>
        def readTwoObjects(idx: Int): Seq[BasicTrackedObjectUpdate] = {
          // We have to carefully pull these out.
          var x1 = buf(idx).toInt
          var y1 = buf(idx+1).toInt
          var x2 = buf(idx+3).toInt
          var y2 = buf(idx+4).toInt
          val extras = buf(idx+2)
          x1 += ((extras & 0x30) << 4)
          y1 += ((extras & 0xc0) << 2)
          x2 += ((extras & 0x03) << 4)
          y2 += ((extras & 0x0c) << 2)
          Seq(BasicTrackedObjectUpdate(x1, y1), BasicTrackedObjectUpdate(x2, y2))
        }
        readTwoObjects(0) ++ readTwoObjects(5)
      case n =>
        // Error!
        throw new CameraException(s"Failed to read basic positions from camera, only found $n bytes available")
    }
  }

  /** Reads the camera positions when the camera is in extended reporting mode. */
  private def readPositionsExtended(): Seq[ExtendedTrackedObjectUpdate] = {
    clearBuf()
    readCamera.read(0, buf, 0, 12) match {
      case 6 =>
        for(i <- 0 until 4) yield readExtendedObjectFromBuf(i*3)
      case n =>
        // Error!
        throw new CameraException(s"Failed to read extended positions from camera, only found $n bytes available")
    }
  }
  // Extended Object format
  private def readExtendedObjectFromBuf(idx: Int) = {
    var x = buf(idx).toInt
    var y = buf(idx+1).toInt
    val xys = buf(idx+2)
    val size = xys & 0x0F
    x += ((xys & 0x30) << 4)
    y += ((xys & 0xc0) << 2)
    ExtendedTrackedObjectUpdate(x, y, size)
  }
}
object TrackingCamera {
  private val ReadDeviceId = 0x58
  private val SendDeviceId = 0xb0
}

class CameraException(msg: String) extends RuntimeException(msg)
