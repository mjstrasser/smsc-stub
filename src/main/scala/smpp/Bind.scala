package smpp

import akka.util.ByteString

case class BindBody(systemId: String, password: String, systemType: String,
                    interfaceVersion: Byte, addrTon: Byte, addrNpi: Byte, addressRange: String)
abstract class Bind(header: Header, bindBody: BindBody) extends Pdu
abstract class BindResp(header: Header, systemId: String) extends Pdu

case class BindTransmitter(header: Header, bindBody: BindBody) extends Bind(header, bindBody)
case class BindTransmitterResp(header: Header, systemId: String) extends BindResp(header, systemId)

object Bind {
  def parseBody(data: ByteString): BindBody = {
    val (systemId, rem) = Pdu.parseString(data)
    val (password, rem2) = Pdu.parseString(rem)
    val (systemType, rem3) = Pdu.parseString(rem2)
    val iterator = rem3.iterator
    val interfaceVersion = iterator.getByte
    val addrTon = iterator.getByte
    val addrNpi = iterator.getByte
    val (addressRange, _) = Pdu.parseString(iterator.toByteString)
    new BindBody(systemId, password, systemType, interfaceVersion, addrTon, addrNpi, addressRange)
  }
}