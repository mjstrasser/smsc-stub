package smpp

import akka.util.{ByteIterator, ByteString}
import Pdu._

case class BindBody(systemId: String, password: String, systemType: String, interfaceVersion: Byte,
                    addrTon: Byte, addrNpi: Byte, addressRange: String) extends Body {
  def toByteString = nullTermString(systemId) ++ nullTermString(password) ++
    nullTermString(systemType) ++ ByteString(interfaceVersion, addrTon, addrNpi) ++
    nullTermString(addressRange)
}
case class BindRespBody(systemId: String) extends Body {
  def toByteString = nullTermString(systemId)
}

abstract class BindRequest extends Pdu
abstract class BindResponse extends Pdu

case class BindTransmitter(header: Header, body: BindBody) extends BindRequest
case class BindTransmitterResp(header: Header, body: BindRespBody) extends BindResponse

case class BindReceiver(header: Header, body: BindBody) extends BindRequest
case class BindReceiverResp(header: Header, body: BindRespBody) extends BindResponse

case class BindTransceiver(header: Header, body: BindBody) extends BindRequest
case class BindTransceiverResp(header: Header, body: BindRespBody) extends BindResponse

case class Unbind(header: Header, body: EmptyBody) extends BindRequest
case class UnbindResp(header: Header, body: EmptyBody) extends BindResponse

case class EnquireLink(header: Header, body: EmptyBody) extends Pdu
case class EnquireLinkResp(header: Header, body: EmptyBody) extends Pdu

object Bind {

  def respBody(systemId: String) = BindRespBody(systemId)

  def getBody(iter: ByteIterator): BindBody = {
    val systemId = getNullTermString(iter)
    val password = getNullTermString(iter)
    val systemType = getNullTermString(iter)
    val interfaceVersion = iter.getByte
    val addrTon = iter.getByte
    val addrNpi = iter.getByte
    val addressRange = getNullTermString(iter)
    BindBody(systemId, password, systemType, interfaceVersion, addrTon, addrNpi, addressRange)
  }
}