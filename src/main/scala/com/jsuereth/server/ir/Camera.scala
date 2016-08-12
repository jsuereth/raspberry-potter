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
  // Obtain the camera on i2c
  private val device = bus.getDevice(ReadDeviceId)
  // Create a buffer for pulling in the values on i2c
  private val buf = new Array[Byte](36)
  /** Initializes the camera hardware. */
  def init(): Unit = {
    // TODO - figure out what all these are and document.
    device.write(0x30, 0x01.toByte)
    Thread.sleep(10)
    // enable the camera.
    device.write(0x30, 0x08.toByte)
    Thread.sleep(10)
    setSensitivtyMax()

    // tell the camera we are ready for data...
    device.write(0x33, 0x33.toByte)
  }

  def setSensitivityMarcan(): Unit = {
    // Block 1
    device.write(0x06, 0x90.toByte)
    Thread.sleep(10)
    device.write(0x08, 0xC0.toByte)
    Thread.sleep(10)
    // Block 2
    device.write(0x1A, 0x40.toByte)
  }

  def setSensitivtyHigh(): Unit = {
    // Block 1
    device.write(0x06, 0x90.toByte)
    Thread.sleep(10)
    device.write(0x08, 0x41.toByte)
    Thread.sleep(10)
    // Block 2
    device.write(0x1A, 0x40.toByte)
  }

  def setSensitivtyMax(): Unit = {
    // Block 1
    device.write(0x06, 0xFF.toByte)
    Thread.sleep(10)
    device.write(0x08, 0x0C.toByte)
    Thread.sleep(10)
    // Block 2
    device.write(0x1A, 0x00.toByte)
  }

  def readPosition(): Seq[TrackedObjectUpdate] = {
    // TODO - example code shows reading 16 bytes but w/ extended object positions.....
    readPositionsExtended()
  }

  private def clearBuf(): Unit = java.util.Arrays.fill(buf, 0, 16, 0.toByte)
  private def askForPositions(): Unit = device.write(PositionAddress.toByte)
  private def readPositionsExtended(): Seq[ExtendedTrackedObjectUpdate] = {
    askForPositions()
    clearBuf()
    device.read(PositionAddress, buf, 0, 16) match {
      case 16 =>
        for(i <- 0 until 4) yield readExtendedObjectFromBuf(1 + (i*3))
      case n =>
        throw new CameraException(s"Failed to read basic positions from camera, only found $n bytes available")
    }
  }
  // Note: Ths is untested!
  /** Reads camera positions when the camera is in basic reporting mode. */
  private def readPositionsBasic(): Seq[BasicTrackedObjectUpdate] = {
    askForPositions
    clearBuf()
    Thread.sleep(2L)
    device.read(PositionAddress, buf, 0, 10) match {
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
  /** converts a byte to an integer, but assumes unsigned values. */
  private def toUint(x: Byte): Int = {
    val t = x.toInt
    if (t < 0) t + 256
    else t
  }
  // Extended Object format.  We allow idx to point us at a random spot in the buffer, since we're not sure
  // what the header might be AND we need to pull 4 possible tracked objects.
  private def readExtendedObjectFromBuf(idx: Int) = {
    var x = toUint(buf(idx))
    var y = toUint(buf(idx+1))
    val xys = toUint(buf(idx+2))
    val size = xys & 0x0F
    x += ((xys & 0x30) << 4)
    y += ((xys & 0xc0) << 2)
    ExtendedTrackedObjectUpdate(x, y, size)
  }
}
object TrackingCamera {
  private val ReadDeviceId = 0x58
  private val PositionAddress = 0x36
}

class CameraException(msg: String) extends RuntimeException(msg)
