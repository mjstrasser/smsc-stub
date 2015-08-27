package smpp

import akka.util.{ByteIterator, ByteString}
import smpp.Pdu._

case class CancelBody(serviceType: String, messageId: String,
                      sourceAddrTon: Byte, sourceAddrNpi: Byte, sourceAddr: String,
                      destAddrTon: Byte, destAddrNpi: Byte, destinationAddr: String) extends Body {
  def toByteString = nullTermString(serviceType) ++ nullTermString(messageId) ++
    ByteString(sourceAddrTon, sourceAddrNpi) ++ nullTermString(sourceAddr) ++
    ByteString(destAddrTon, destAddrNpi) ++ nullTermString(destinationAddr)
  override def toString = s"(id: $messageId source: $sourceAddr dest: $destinationAddr)"
}

case class CancelSm(header: Header, body: CancelBody) extends Pdu {
  val name = "cancel"
}

case class CancelSmResp(header: Header, body: EmptyBody) extends Pdu {
  val name = "cancel_resp"
}

object Cancel {

  def parseBody(iter: ByteIterator) = {
    val serviceType = parseNullTermString(iter)
    val messageId = parseNullTermString(iter)
    val sourceAddrTon = iter.getByte
    val sourceAddrNpi = iter.getByte
    val sourceAddr = parseNullTermString(iter)
    val destAddrTon = iter.getByte
    val destAddrNpi = iter.getByte
    val destinationAddr = parseNullTermString(iter)
    CancelBody(serviceType, messageId, sourceAddrTon, sourceAddrNpi, sourceAddr,
      destAddrTon, destAddrNpi, destinationAddr)
  }
  
}
