package com.jsuereth.server.collections

import org.specs2._
/**
  * Created by jsuereth on 9/24/16.
  */
class CircularBufferSpec extends Specification { def is = s2"""
A CircularBuffer should
  replace old values                 $replaceOldValue
  have size equal to placed elements $haveSizeEqualToPlaceElements
  """


  def replaceOldValue = {
    val x = CircularBuffer.withSize[Int](2)
    for (i <- 0 until 12) x put i
    x must contain(10, 11)
  }
  def haveSizeEqualToPlaceElements = {
    val x = CircularBuffer.withSize[Int](10)
    for (i <- 0 until 2) x put i
    x must haveSize(2)
  }
}
