package com.jsuereth.server.ir

/** An update from the IR tracking camera on a specific object. */
sealed trait TrackedObjectUpdate {
  /** the x position of an object. (0-1023) */
  def x: Int
  /** the y position of an object. (0-767) */
  def y: Int
  /** whether or not the reported position should be considered "empty" (i.e. no data). */
  def isEmpty: Boolean
}
object TrackedObjectUpdate {
  val empty: TrackedObjectUpdate = ExtendedTrackedObjectUpdate(1023, 1023, 15)
}

/** A basic update.  Only has (x,y) position in resolution 1024x768. */
case class BasicTrackedObjectUpdate(x: Int, y: Int) extends TrackedObjectUpdate {
  override def toString = s"? @ (${x}, ${y})"
  override def isEmpty: Boolean = (x == 1023) && (y == 1023)
}
/**
  * An extended position.
  *
  * Has the location (x,y) in resolution 1024x768 space in addiiton to the "size" of the object, in 0-15 space.
  */
case class ExtendedTrackedObjectUpdate(x: Int, y: Int, size: Int) extends TrackedObjectUpdate {
  override def toString = s"$size @ (${x}, ${y})"
  override def isEmpty: Boolean = (x == 1023) && (y == 1023) && (size == 15)
}