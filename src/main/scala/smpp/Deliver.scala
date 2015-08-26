package smpp

import java.util.concurrent.atomic.AtomicInteger

import akka.util.{ByteIterator, ByteString}
import smpp.Pdu._
import smsc.MoMessage

// TODO: Combine DeliverBody with SubmitBody because they are the same.

case class DeliverBody(serviceType: String, sourceAddrTon: Byte, sourceAddrNpi: Byte, sourceAddr: String,
                      destAddrTon: Byte, destAddrNpi: Byte, destinationAddr: String,
                      esmClass: Byte, protocolId: Byte, priorityFlag: Byte,
                      scheduleDeliveryTime: String, validityPeriod: String,
                      registeredDelivery: Byte, replaceIfPresentFlag: Byte, dataCoding: Byte,
                      smDefaultMsgId: Byte, smLength: Byte, shortMessage: String) extends Body {
  def toByteString = nullTermString(serviceType) ++
    ByteString(sourceAddrTon, sourceAddrNpi) ++ nullTermString(sourceAddr) ++
    ByteString(destAddrTon, destAddrNpi) ++ nullTermString(destinationAddr) ++
    ByteString(esmClass, protocolId, priorityFlag) ++
    nullTermString(scheduleDeliveryTime) ++ nullTermString(validityPeriod) ++
    ByteString(registeredDelivery, replaceIfPresentFlag, dataCoding, smDefaultMsgId, smLength) ++
    nullTermString(shortMessage)
  override def toString = s"(source: $sourceAddr dest: $destinationAddr msg: $shortMessage)"
}

case class DeliverRespBody(messageId: String) extends Body {
  def toByteString = nullTermString(messageId)
  override def toString = s"(id: $messageId)"
}

case class DeliverSm(header: Header, body: DeliverBody) extends Pdu {
  val name = "deliver"
}

case class DeliverSmResp(header: Header, body: DeliverRespBody) extends Pdu {
  val name = "deliver_resp"
}

object Deliver {

  def getBody(iter: ByteIterator): DeliverBody = {
    val serviceType = parseNullTermString(iter)
    val sourceAddrTon = iter.getByte
    val sourceAddrNpi = iter.getByte
    val sourceAddr = parseNullTermString(iter)
    val destAddrTon = iter.getByte
    val destAddrNpi = iter.getByte
    val destinationAddr = parseNullTermString(iter)
    val esmClass = iter.getByte
    val protocolId = iter.getByte
    val priorityFlag = iter.getByte
    val scheduleDeliveryTime = parseNullTermString(iter)
    val validityPeriod = parseNullTermString(iter)
    val registeredDelivery = iter.getByte
    val replaceIfPresentFlag = iter.getByte
    val dataCoding = iter.getByte
    val smDefaultMsgId = iter.getByte
    val smLength = iter.getByte
    val shortMessage = parseNullTermString(iter)
    DeliverBody(serviceType, sourceAddrTon, sourceAddrNpi, sourceAddr, destAddrTon, destAddrNpi, destinationAddr,
      esmClass, protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod, registeredDelivery,
      replaceIfPresentFlag, dataCoding, smDefaultMsgId, smLength, shortMessage)
  }

  def respBody(messageId: String) = DeliverRespBody(messageId)
  
  def sm(moMessage: MoMessage) = {
    val header = Header(CommandId.deliver_sm, CommandStatus.NULL, deliverCounter.incrementAndGet)
    val body = DeliverBody("", 1, 1, moMessage.sender,
                           0, 1, moMessage.recipient,
                           0, 0, 1,
                           "", "",
                           0, 0, 0,
                           0, moMessage.message.length.toByte, moMessage.message)
    DeliverSm(header, body)
  }
  
  val deliverCounter = new AtomicInteger(0)

}
