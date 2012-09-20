package org.scala_tools.maven.mojo.util

sealed trait Resource[+T] {
  val value: T
  def close: Unit
  def use[X](f: T => X) = try { f(value) } finally { close }
}

object Resource {
  type Closable = { def close(); }
  
  implicit def pimpClosable[A <: Closable](x : A) = new Resource[A] {
    override val value = x
    override def close = x.close()    
  }
}

import java.io._
object RichBufferedReader {
  implicit def pimpBufferedReader(reader : BufferedReader) = new Iterable[String] {
    override def iterator = new BufferedReaderIterator(reader)
  }
}

class BufferedReaderIterator(reader : BufferedReader) extends Iterator[String] {
  private[this] var isNextCalled = false
  private[this] var currentLine : String = null  
  private def getNextLine(reset : Boolean) = {
    if(!isNextCalled) {
      currentLine = reader.readLine
      isNextCalled = true
    }    
    if(reset) {
      isNextCalled = false
    }
    currentLine
  }
  
  def hasNext = getNextLine(false) != null
  def next() = getNextLine(true)
}