package com.jsuereth.server.collections

import scala.reflect.ClassTag

/**
  * A circular buffer.  Will overwrite "old" data when new data comes in.
  */
class CircularBuffer[T] private (buf: Array[T]) extends Iterable[T] {
  private var cursor: Int = 0
  private var curSize: Int = 0

  /** Checks whether or not the buffer is empty. */
  override def isEmpty: Boolean = size == 0
  /** Pushes another value into the circular buffer.  This will overwrite old data if the buffer is full. */
  def put(e: T): this.type = {
    buf(cursor) = e
    cursor = (cursor + 1) % buf.size
    if (curSize < buf.size) curSize += 1
    this
  }
  /** Returns an iterator over the data in this buffer. */
  def iterator: Iterator[T] = new Iterator[T] {
    private var idx = 0
    override def hasNext: Boolean =
      idx < curSize
    override def next(): T = {
      val realIdx = (cursor + idx) % buf.size
      idx += 1
      buf(realIdx)
    }
  }
}
object CircularBuffer {
  def withSize[T](size: Int)(implicit classTag: ClassTag[T]): CircularBuffer[T] =
    new CircularBuffer[T](new Array[T](size))
}
