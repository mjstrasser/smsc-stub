package smpp

import akka.util.ByteIterator
import smpp.Pdu._

case class CancelBody(serviceType: String, messageId: String,
                      source: Address, dest: Address) extends Body {
  def toByteString = nullTermString(serviceType) ++ nullTermString(messageId) ++
    source.toByteString ++ dest.toByteString
  override def toString = s"(id: $messageId source: $source dest: $dest)"
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
    val source = Address.parseAddress(iter)
    val dest = Address.parseAddress(iter)
    CancelBody(serviceType, messageId, source, dest)
  }
  
}
