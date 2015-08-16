package smsc

import smpp._
import Pdu._
import Bind._
import CommandStatus._

object Stub {
  def responseTo(request: Pdu) = {
    request match {
      case BindTransmitter(header, body) => BindTransmitterResp(respHeader(header, NULL), respBody(body.systemId))
      case BindReceiver(header, body) => BindReceiverResp(respHeader(header, NULL), respBody(body.systemId))
      case BindTransceiver(header, body) => BindTransceiverResp(respHeader(header, NULL), respBody(body.systemId))
      case Unbind(header, body) => Unbind(respHeader(header, NULL), body)
      case EnquireLink(header, body) => EnquireLinkResp(respHeader(header, ESME_ROK), body)
    }
  }
}
