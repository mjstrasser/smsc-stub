package smpp

import java.util.concurrent.atomic.AtomicInteger

import akka.util.{ByteIterator, ByteString}
import smpp.Pdu._
import smsc.MoMessage

// TODO: Combine DeliverBody with SubmitBody because they are the same.

/**
 * The body of a deliver request PDU sent to an ESME from the SMSC.
 *
 * Currently without any optional parameters.
 */
case class DeliverBody(serviceType: String, source: Address, dest: Address,
                      esmClass: Byte, protocolId: Byte, priorityFlag: Byte,
                      scheduleDeliveryTime: String, validityPeriod: String,
                      registeredDelivery: Byte, replaceIfPresentFlag: Byte, dataCoding: Byte,
                      smDefaultMsgId: Byte, smLength: Byte, shortMessage: String,
                      optionals: Seq[Tlv[TlValue]]) extends Body {
  def toByteString = cString(serviceType) ++
    source.toByteString ++ dest.toByteString ++
    ByteString(esmClass, protocolId, priorityFlag) ++
    cString(scheduleDeliveryTime) ++ cString(validityPeriod) ++
    ByteString(registeredDelivery, replaceIfPresentFlag, dataCoding, smDefaultMsgId, smLength) ++
    octetString(shortMessage) ++ optionals.flatMap(_.toByteString)
  override def toString = {
    val opts = optionals.map(_.toString).mkString(" ")
    s"(source: $source dest: $dest msg: '$shortMessage' esm: $esmClass $opts)"
  }
}

/**
 * The SMPP `deliver_sm_resp` PDU has a one-byte body that is always null.
 */
case class DeliverRespBody(id: Byte) extends Body {
  def toByteString = ByteString(id)
  override def toString = s"(id: $id)"
}

case class DeliverSm(header: Header, body: DeliverBody) extends Pdu {
  val name = "deliver"
}

case class DeliverSmResp(header: Header, body: DeliverRespBody) extends Pdu {
  val name = "deliver_resp"
}

object Deliver {

  import Address._

  def parseBody(iter: ByteIterator): DeliverBody = {
    val serviceType = parseCString(iter)
    val source = parseAddress(iter)
    val dest = parseAddress(iter)
    val esmClass = iter.getByte
    val protocolId = iter.getByte
    val priorityFlag = iter.getByte
    val scheduleDeliveryTime = parseCString(iter)
    val validityPeriod = parseCString(iter)
    val registeredDelivery = iter.getByte
    val replaceIfPresentFlag = iter.getByte
    val dataCoding = iter.getByte
    val smDefaultMsgId = iter.getByte
    val smLength = iter.getByte
    val shortMessage = parseOctetString(iter, smLength)
    DeliverBody(serviceType, source, dest,
      esmClass, protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod, registeredDelivery,
      replaceIfPresentFlag, dataCoding, smDefaultMsgId, smLength, shortMessage, Seq())
  }
  
  def parseRespBody(iter: ByteIterator): DeliverRespBody = new DeliverRespBody(iter.getByte)

  def sm(moMessage: MoMessage) = {
    val header = Header(CommandId.deliver_sm, CommandStatus.NULL, deliverCounter.incrementAndGet)
    val body = DeliverBody("", Address(moMessage.sender), Address(moMessage.recipient),
                           0, 0, 1,
                           "", "",
                           0, 0, 0,
                           0, moMessage.message.length.toByte, moMessage.message, Seq())
    DeliverSm(header, body)
  }

  def deliveryReceipt(submitBody: SubmitBody, messageId: String) = {
    val header = Header(CommandId.deliver_sm, CommandStatus.NULL, deliverCounter.incrementAndGet)
    val body = DeliverBody(submitBody.serviceType, submitBody.dest, submitBody.source,
                           EsmClass.smsc_delivery_rcpt, 0, 1,
                           "", "",
                           0, 0, 0,
                           0, submitBody.shortMessage.length.toByte, submitBody.shortMessage,
                           Seq(Optionals.messageStateDelivered, Optionals.receiptedMessageId(messageId)))
    DeliverSm(header, body)
  }
  
  val deliverCounter = new AtomicInteger(0)

}
