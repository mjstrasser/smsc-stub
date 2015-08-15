package smpp

import akka.util.{ByteIterator, ByteString}
import Pdu._

case class BindBody(systemId: String, password: String, systemType: String, interfaceVersion: Byte,
                    addrTon: Byte, addrNpi: Byte, addressRange: String) extends Body {
  override def toByteString = nullTermString(systemId) ++ nullTermString(password) ++
    nullTermString(systemType) ++ ByteString(interfaceVersion, addrTon, addrNpi) ++
    nullTermString(addressRange)
}
case class BindRespBody(systemId: String) extends Body {
  override def toByteString = nullTermString(systemId)
}

abstract class BindRequest extends Pdu
abstract class BindResponse extends Pdu

case class BindTransmitter(header: Header, body: BindBody) extends BindRequest
case class BindTransmitterResp(header: Header, body: BindRespBody) extends BindResponse

object Bind {

  def respBody(systemId: String) = new BindRespBody(systemId)

  def getBody(iter: ByteIterator): BindBody = {
    val systemId = Pdu.getNullTermString(iter)
    val password = Pdu.getNullTermString(iter)
    val systemType = Pdu.getNullTermString(iter)
    val interfaceVersion = iter.getByte
    val addrTon = iter.getByte
    val addrNpi = iter.getByte
    val addressRange = Pdu.getNullTermString(iter)
    new BindBody(systemId, password, systemType, interfaceVersion, addrTon, addrNpi, addressRange)
  }
}