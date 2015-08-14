package smpp

case class BindBody(systemId: String, password: String, systemType: String,
                    interfaceVersion: Byte, addrTon: Byte, addrNpi: Byte, addressRange: String)
abstract class Bind(header: Header, bindBody: BindBody)
abstract class BindResp(header: Header, systemId: String)

case class BindTransmitter(header: Header, bindBody: BindBody) extends Bind(header, bindBody)
case class BindTransmitterResp(header: Header, systemId: String) extends BindResp(header, systemId)

object Bind {

}