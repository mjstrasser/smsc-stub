package smpp

import Pdu._
import akka.util.{ByteString, ByteIterator}

case class SubmitBody(serviceType: String, sourceAddrTon: Byte, sourceAddrNpi: Byte, sourceAddr: String,
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
}

case class SubmitRespBody(messageId: String) extends Body {
  def toByteString = nullTermString(messageId)
}

case class SubmitSm(header: Header, body: SubmitBody) extends Pdu

case class SubmitSmResp(header: Header, body: SubmitRespBody) extends Pdu

object Submit {

  def getBody(iter: ByteIterator): SubmitBody = {
    val serviceType = getNullTermString(iter)
    val sourceAddrTon = iter.getByte
    val sourceAddrNpi = iter.getByte
    val sourceAddr = getNullTermString(iter)
    val destAddrTon = iter.getByte
    val destAddrNpi = iter.getByte
    val destinationAddr = getNullTermString(iter)
    val esmClass = iter.getByte
    val protocolId = iter.getByte
    val priorityFlag = iter.getByte
    val scheduleDeliveryTime = getNullTermString(iter)
    val validityPeriod = getNullTermString(iter)
    val registeredDelivery = iter.getByte
    val replaceIfPresentFlag = iter.getByte
    val dataCoding = iter.getByte
    val smDefaultMsgId = iter.getByte
    val smLength = iter.getByte
    val shortMessage = getNullTermString(iter)
    SubmitBody(serviceType, sourceAddrTon, sourceAddrNpi, sourceAddr, destAddrTon, destAddrNpi, destinationAddr,
      esmClass, protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod, registeredDelivery,
      replaceIfPresentFlag, dataCoding, smDefaultMsgId, smLength, shortMessage)
  }

  def respBody(messageId: String) = SubmitRespBody(messageId)

}
