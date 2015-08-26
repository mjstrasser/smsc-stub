package smpp

import akka.util.{ByteIterator, ByteString}
import smpp.Pdu._

/**
 * The body of a submit request PDU.
 *
 * Currently without any optional parameters.
 *
 * @param serviceType
 * @param sourceAddrTon
 * @param sourceAddrNpi
 * @param sourceAddr
 * @param destAddrTon
 * @param destAddrNpi
 * @param destinationAddr
 * @param esmClass
 * @param protocolId
 * @param priorityFlag
 * @param scheduleDeliveryTime
 * @param validityPeriod
 * @param registeredDelivery
 * @param replaceIfPresentFlag
 * @param dataCoding
 * @param smDefaultMsgId
 * @param smLength
 * @param shortMessage
 */
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
  override def toString = s"(source: $sourceAddr dest: $destinationAddr msg: $shortMessage)"
}

case class SubmitRespBody(messageId: String) extends Body {
  def toByteString = nullTermString(messageId)
  override def toString = s"(id: $messageId)"
}

case class SubmitSm(header: Header, body: SubmitBody) extends Pdu {
  val name = "submit"
}

case class SubmitSmResp(header: Header, body: SubmitRespBody) extends Pdu {
  val name = "submit_resp"
}

object Submit {

  /**
   * Parses the body of a submit PDU from bytes.
   *
   * @param iter iterator over the bytes with the body
   * @return the body of the PDU
   */
  def parseBody(iter: ByteIterator): SubmitBody = {
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
    SubmitBody(serviceType, sourceAddrTon, sourceAddrNpi, sourceAddr, destAddrTon, destAddrNpi, destinationAddr,
      esmClass, protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod, registeredDelivery,
      replaceIfPresentFlag, dataCoding, smDefaultMsgId, smLength, shortMessage)
  }

  def respBody(messageId: String) = SubmitRespBody(messageId)

}
