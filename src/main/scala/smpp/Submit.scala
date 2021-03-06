package smpp

import akka.util.{ByteIterator, ByteString}
import smpp.Pdu._

/**
 * The body of a submit request PDU sent from an ESME to the SMSC.
 *
 * Currently without any optional parameters.
 */
case class SubmitBody(serviceType: String, source: Address, dest: Address,
                      esmClass: Byte, protocolId: Byte, priorityFlag: Byte,
                      scheduleDeliveryTime: String, validityPeriod: String,
                      registeredDelivery: Byte, replaceIfPresentFlag: Byte, dataCoding: Byte,
                      smDefaultMsgId: Byte, smLength: Byte, shortMessage: String) extends Body {
  def toByteString = cString(serviceType) ++
    source.toByteString ++ dest.toByteString ++
    ByteString(esmClass, protocolId, priorityFlag) ++
    cString(scheduleDeliveryTime) ++ cString(validityPeriod) ++
    ByteString(registeredDelivery, replaceIfPresentFlag, dataCoding, smDefaultMsgId, smLength) ++
    octetString(shortMessage)
  override def toString = s"(source: $source dest: $dest msg: '$shortMessage' reg_deliv: $registeredDelivery)"
}

case class SubmitRespBody(messageId: String) extends Body {
  def toByteString = cString(messageId)
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
    val serviceType = parseCString(iter)
    val source = Address.parseAddress(iter)
    val dest = Address.parseAddress(iter)
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
    SubmitBody(serviceType, source, dest,
      esmClass, protocolId, priorityFlag, scheduleDeliveryTime, validityPeriod, registeredDelivery,
      replaceIfPresentFlag, dataCoding, smDefaultMsgId, smLength, shortMessage)
  }

  def respBody(messageId: String) = SubmitRespBody(messageId)

}
