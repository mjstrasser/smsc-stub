package smpp

import akka.util.{ByteString, ByteStringBuilder}
import smpp.Pdu._

trait TlValue {
  implicit val byteOrder = java.nio.ByteOrder.BIG_ENDIAN
}
case class TlByte(value: Byte) extends TlValue
case class TlShort(value: Short) extends TlValue
case class TlInt(value: Int) extends TlValue
case class TlCString(value: String) extends TlValue
case class TlOctetString(value: String) extends TlValue

abstract class Tlv[+T] {
  def tag: Short
  def toByteString: ByteString
  def toStringTemplate = s"(opt: $tag %d %s)"
}

case class TlvByte(tag: Short, value: Byte) extends Tlv[TlByte] {
  def toByteString = new ByteStringBuilder()
    .putShort(tag)
    .putShort(1)
    .putByte(value)
    .result()
  override def toString = toStringTemplate.format(1, value)
}

case class TlvShort(tag: Short, value: Short) extends Tlv[TlShort] {
  def toByteString = new ByteStringBuilder()
    .putShort(tag)
    .putShort(2)
    .putShort(value)
    .result()
  override def toString = toStringTemplate.format(2, value)
}

case class TlvInt(tag: Short, value: Int) extends Tlv[TlInt] {
  def toByteString = new ByteStringBuilder()
    .putShort(tag)
    .putShort(4)
    .putInt(value)
    .result()
  override def toString = toStringTemplate.format(4, value)
}

case class TlvCString(tag: Short, value: String) extends Tlv[TlCString] {
  val length = (value.length + 1).toShort
  def toByteString = new ByteStringBuilder()
    .putShort(tag)
    .putShort(length)
    .result() ++ nullTermString(value)
  override def toString = toStringTemplate.format(length, s"'$value'")
}

case class TlvOctetString(tag: Short, value: String) extends Tlv[TlOctetString] {
  val length = value.length.toShort
  def toByteString = new ByteStringBuilder()
    .putShort(tag)
    .putShort(length)
    .result() ++ octetString(value)
  override def toString = toStringTemplate.format(length, s"'$value'")
}

