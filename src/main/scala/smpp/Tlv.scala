package smpp

import akka.util.{ByteString, ByteStringBuilder}

abstract class Tlv(val tag: Short, val length: Short) {
  def toByteString: ByteString
}

object Tlv {

  case class user_message_reference(value: Short) extends Tlv(0x0204, 2) {
    def toByteString = Tlv.toByteString(tag, length, value)
  }

  import Pdu._

  def toByteString(tag: Short, length: Short, value: Short) = new ByteStringBuilder()
    .putShort(tag)
    .putShort(length)
    .putShort(value)
    .result()
}