package com.jsuereth.server.mic

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.concurrent.BlockingQueue

import javax.sound.sampled._

import scala.collection.mutable.ArrayBuffer

/** Helper class for dealing witht he microphone.  Will open/close and read samples into a buffer. */
class Microphone(targetDataLine: TargetDataLine, format: AudioFormat) {
  /**
    * Reads the microphone, using the passed in buffer (overwritting it) until the volume meets the given condition.
    * @param buf
    * @param The condition for when to stop.
    */
  def readUntilCondition(buf: Array[Byte])(condition: Double => Boolean): Unit = {
    val opened = !targetDataLine.isOpen
    if (opened) targetDataLine.open(format)
    targetDataLine.start()
    val correlationBuf = new Array[Int](buf.length)
    def read(): Unit =
      targetDataLine.read(buf, 0, buf.length) match {
        case 0 if targetDataLine.isOpen => read()
        case 0 => () // Line is closed, no input, stop reading.
        case n =>
          val correlation = Vad.rawAutocorrelation(buf, correlationBuf)
          // Stop when we meet the condition.
          if (condition(correlation)) ()
          else read()
      }
    try read()
    finally {
      targetDataLine.stop()
      // Close the line if we opened it.
      if (opened) targetDataLine.close()
    }
  }
}

object Constants {
  val SAMPLE_RATE = 1600 // 22000?
  val BYTES_PER_BUFFER = SAMPLE_RATE * 1 // should be *4 ?
}

object Volume {
  // Calculates the RMS volume level of some input data.   We use this
  // to detect silence over time.
  def calculateRMSLevel(audioData: Array[Byte], length: Int): Int = {
    val sum = audioData.iterator.take(length).sum
    val avg = sum / length.toDouble
    val sumMeanSq =
      audioData.iterator.take(length).map(i => Math.pow(i-avg, 2)).sum
    val avgMeanSq = sumMeanSq / length.toDouble
    (Math.pow(avgMeanSq, 0.5d) + 0.5).toInt
  }
}

object MicTest {
  def main(args: Array[String]): Unit= {
    val audioFormat = new AudioFormat(Constants.SAMPLE_RATE, 16, 1, true, false)
    // A target info of a format we want.
    val targetInfo = new DataLine.Info(classOf[TargetDataLine], audioFormat)
    System.out.println("Audio Info")
    val findLine = for {
      mixerInfo <- AudioSystem.getMixerInfo.toIterator
      _ = println(s"determining if we can use ${mixerInfo}")
      mixer = AudioSystem.getMixer(mixerInfo)
      if mixerInfo.getName contains "Yeti"
      lineInfo <- mixer.getSourceLineInfo
      if AudioSystem.isLineSupported(lineInfo)
      _ = println(s"\tdetermining if we can use ${lineInfo}")
      line = mixer.getLine(lineInfo)
      if line.isInstanceOf[TargetDataLine]
    } yield line.asInstanceOf[TargetDataLine]

    val line =
      if (findLine.hasNext) findLine.next
      else AudioSystem.getLine(targetInfo).asInstanceOf[TargetDataLine]

    if (AudioSystem.isLineSupported(line.getLineInfo)) {
      System.err.println(s"Connecting to Microphone (${line.getLineInfo})...")
      runMic(line, line.getFormat)
    } else System.err.println("Microphone not supported!")
  }
  // TODO - we need to take a "baseline" RMS noise sample to see when the user goes over the baseline at the start.
  def runMic(targetDataLine: TargetDataLine, audioFormat: AudioFormat): Unit = {

    System.err.println(s"Opening Microphone ${targetDataLine}...")
    val mic = new Microphone(targetDataLine, audioFormat)

    // TODO - move the following (record until silent) into the microphone class.
    val data = new Array[Byte](Constants.BYTES_PER_BUFFER)
    var started = false
    val output = new ByteArrayOutputStream()
    def recordBuf(): Unit = {
      output.write(data, 0, data.length)
    }
    def saveOutput(): Unit = {
      val file = new java.io.File("test.wav")
      val allSound = output.toByteArray
      val bais = new ByteArrayInputStream(allSound)
      val audioInputStream = new AudioInputStream(bais, audioFormat, allSound.length / audioFormat.getFrameSize)
      AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file)
      audioInputStream.close()
      output.close()
      System.out.println(s"Wrote audio to ${file.getAbsolutePath}")
    }
    mic.readUntilCondition(data) { correlation =>
      System.out.println(s"Sampled audio of volume: ${correlation}, silent volume: ${Vad.THRESHOLD}")
      if (started) {
        recordBuf()
        // Stop after the volume goes below the background noise
        correlation < Vad.THRESHOLD
      } else {
        if (correlation >Vad.THRESHOLD) {
          System.out.println("Detected audio start!")
          started = true
          recordBuf()
        }
        false
      }
    }
    System.out.print(s"Done!")
    saveOutput()
  }
}
