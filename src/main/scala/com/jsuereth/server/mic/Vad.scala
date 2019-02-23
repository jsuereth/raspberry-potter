package com.jsuereth.server.mic

import java.util

/** Simplified voice activity detection.  Uses autocorrelation. */
object Vad {

  val THRESHOLD = 400
  /**
    * @param samples
    * @param correlationBuffer A buffer we use to compute the correlation
    * @return
    *         The mean correlation value.
    */
  def rawAutocorrelation(samples: Array[Byte], correlationBuffer: Array[Int]): Double = {
    util.Arrays.fill(correlationBuffer, 0)
    for {
      j <- 0 until samples.length
      i <- 0 until samples.length
    } correlationBuffer(i) += (samples(i) * samples((samples.length + i - j) % samples.length))
    correlationBuffer.iterator.sum / correlationBuffer.length.toDouble
  }

  def rawIsVoice(samples: Array[Byte], correlationBuffer: Array[Int]): Boolean = {
    rawAutocorrelation(samples, correlationBuffer) > THRESHOLD
  }
}
