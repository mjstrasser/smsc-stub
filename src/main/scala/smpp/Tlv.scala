package smpp

import akka.util.{ByteIterator, ByteString, ByteStringBuilder}
import smpp.Pdu._

trait TlValue {
  implicit val byteOrder = java.nio.ByteOrder.BIG_ENDIAN
}
case class TlByte(value: Byte) extends TlValue
case class TlShort(value: Short) extends TlValue
case class TlInt(value: Int) extends TlValue
case class TlCString(value: String) extends TlValue
case class TlOctetString(value: String) extends TlValue
case class TlNone() extends TlValue

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
    .result() ++ cString(value)
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

case class TlvNone(tag: Short) extends Tlv[TlNone] {
  val length = 0
  def toByteString = new ByteStringBuilder()
    .putShort(tag)
    .putShort(length)
    .result()
  override def toString = toStringTemplate.format(length, "")
}

object Tlv extends TlValue {
  import Optionals._
  def parseTlv(iterator: ByteIterator): Tlv[TlValue] = {
    val tag = iterator.getShort
    val length = iterator.getShort
    tag match {
      case `dest_addr_subunit` => TlvByte(tag, iterator.getByte)
      case `source_addr_subunit` => TlvByte(tag, iterator.getByte)
      case `dest_network_type` => TlvByte(tag, iterator.getByte)
      case `source_network_type` => TlvByte(tag, iterator.getByte)
      case `dest_bearer_type` => TlvByte(tag, iterator.getByte)
      case `source_bearer_type` => TlvByte(tag, iterator.getByte)
      // Not sure if the following two should be byte or short: the
      // spec is inconsistent (sections 5.3.2.7 and 5.3.2.8).
      case `dest_telematics_id` => TlvShort(tag, iterator.getShort)
      case `source_telematics_id` => TlvShort(tag, iterator.getShort)
      case `qos_time_to_live` => TlvInt(tag, iterator.getInt)
      case `payload_type` => TlvByte(tag, iterator.getByte)
      case `additional_status_info_text` => TlvCString(tag, parseCString(iterator))
      case `receipted_message_id` => TlvCString(tag, parseCString(iterator))
      case `ms_msg_wait_facilities` => TlvByte(tag, iterator.getByte)
      case `privacy_indicator` => TlvByte(tag, iterator.getByte)
      case `source_subaddress` => TlvOctetString(tag, parseOctetString(iterator, length))
      case `dest_subaddress` => TlvOctetString(tag, parseOctetString(iterator, length))
      case `user_message_reference` => TlvShort(tag, iterator.getShort)
      case `user_response_code` => TlvByte(tag, iterator.getByte)
      case `language_indicator` => TlvByte(tag, iterator.getByte)
      case `source_port` => TlvShort(tag, iterator.getShort)
      case `destination_port` => TlvShort(tag, iterator.getShort)
      case `sar_msg_ref_num` => TlvShort(tag, iterator.getShort)
      case `sar_total_segments` => TlvByte(tag, iterator.getByte)
      case `sar_segment_seqnum` => TlvByte(tag, iterator.getByte)
      case `SC_interface_version` => TlvByte(tag, iterator.getByte)
      case `display_time` => TlvByte(tag, iterator.getByte)
      case `ms_validity` => TlvByte(tag, iterator.getByte)
      case `dpf_result` => TlvByte(tag, iterator.getByte)
      case `set_dpf` => TlvByte(tag, iterator.getByte)
      case `ms_availability_status` => TlvByte(tag, iterator.getByte)
      case `network_error_code` => TlvOctetString(tag, parseOctetString(iterator, length))
      case `message_payload` => TlvOctetString(tag, parseOctetString(iterator, length))
      case `delivery_failure_reason` => TlvByte(tag, iterator.getByte)
      case `more_messages_to_send` => TlvByte(tag, iterator.getByte)
      case `message_state` => TlvByte(tag, iterator.getByte)
      case `callback_num` => TlvOctetString(tag, parseOctetString(iterator, length))
      case `callback_num_pres_ind` => TlvByte(tag, iterator.getByte)
      case `callback_num_atag` => TlvOctetString(tag, parseOctetString(iterator, length))
      case `number_of_messages` => TlvByte(tag, iterator.getByte)
      case `sms_signal` => TlvShort(tag, iterator.getShort)
      case `alert_on_message_delivery` => TlvNone(tag)
      case `its_reply_type` => TlvByte(tag, iterator.getByte)
      case `its_session_info` => TlvShort(tag, iterator.getShort)
      case `ussd_service_op` => TlvByte(tag, iterator.getByte)
    }
  }
}
