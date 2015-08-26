package smpp

import akka.util.{ByteIterator, ByteString}
import smpp.Pdu._

/**
 * The body of a bind request PDU.
 *
 * @param systemId the ID of the ESME requesting to bind to the SMSC
 * @param password password for the [[systemId]]
 * @param systemType type of the ESME system
 * @param interfaceVersion SMPP version (currently only 3.4: [[Pdu.SmppVersion]])
 * @param addrTon type of Number of the ESME address (may be null)
 * @param addrNpi Numbering Plan Indicator of the ESME address (may be null)
 * @param addressRange range of SME addresses serviced by the ESME
 */
case class BindBody(systemId: String, password: String, systemType: String, interfaceVersion: Byte,
                    addrTon: Byte, addrNpi: Byte, addressRange: String) extends Body {
  def toByteString = nullTermString(systemId) ++ nullTermString(password) ++
    nullTermString(systemType) ++ ByteString(interfaceVersion, addrTon, addrNpi) ++
    nullTermString(addressRange)
}

/**
 * The body of a bind response PDU.
 *
 * @param systemId the ID of the SMSC
 */
case class BindRespBody(systemId: String) extends Body {
  def toByteString = nullTermString(systemId)
  override def toString = s"(id: $systemId)"
}

/** Base class for bind request PDUs. */
abstract class BindRequest extends Pdu
/** Base class for bind repsonse PDUs. */
abstract class BindResponse extends Pdu

/** Class for the SMPP `bind_transmitter` PDU. */
case class BindTransmitter(header: Header, body: BindBody) extends BindRequest {
  val name = "bind_tx"
}
/** Class for the SMPP `bind_transmitter_resp` PDU. */
case class BindTransmitterResp(header: Header, body: BindRespBody) extends BindResponse {
  val name = "bind_tx_resp"
}

/** Class for the SMPP `bind_receiver` PDU. */
case class BindReceiver(header: Header, body: BindBody) extends BindRequest {
  val name = "bind_rx"
}
/** Class for the SMPP `bind_receiver_resp` PDU. */
case class BindReceiverResp(header: Header, body: BindRespBody) extends BindResponse {
  val name = "bind_tx_resp"
}

/** Class for the SMPP `bind_transceiver` PDU. */
case class BindTransceiver(header: Header, body: BindBody) extends BindRequest {
  val name = "bind_trx"
}
/** Class for the SMPP `bind_transceiver_resp` PDU. */
case class BindTransceiverResp(header: Header, body: BindRespBody) extends BindResponse {
  val name = "bind_trx_resp"
}

/** Class for the SMPP `unbind` PDU. */
case class Unbind(header: Header, body: EmptyBody) extends BindRequest {
  val name = "unbind"
}
/** Class for the SMPP `unbind_resp` PDU. */
case class UnbindResp(header: Header, body: EmptyBody) extends BindResponse {
  val name = "unbind"
}

/** Class for the SMPP `enquire_link` PDU. */
case class EnquireLink(header: Header, body: EmptyBody) extends Pdu {
  val name = "bind_tx"
}
/** Class for the SMPP `enquire_link_resp` PDU. */
case class EnquireLinkResp(header: Header, body: EmptyBody) extends Pdu {
  val name = "bind_tx"
}

object Bind {

  /** Creates the body for a bind response PDU. */
  def respBody(systemId: String) = BindRespBody(systemId)

  /**
   * Parses the body of a bind PDU from bytes.
   *
   * @param iter iterator over the bytes with the body
   * @return the body of the PDU
   */
  def parseBody(iter: ByteIterator): BindBody = {
    val systemId = parseNullTermString(iter)
    val password = parseNullTermString(iter)
    val systemType = parseNullTermString(iter)
    val interfaceVersion = iter.getByte
    val addrTon = iter.getByte
    val addrNpi = iter.getByte
    val addressRange = parseNullTermString(iter)
    BindBody(systemId, password, systemType, interfaceVersion, addrTon, addrNpi, addressRange)
  }
}