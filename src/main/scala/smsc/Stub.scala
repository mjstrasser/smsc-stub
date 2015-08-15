package smsc

import smpp._

object Stub {
  def responseTo(request: Pdu) = {
    request match {
      case BindTransmitter(header, body) =>
        BindTransmitterResp(Pdu.respHeader(header, 0), Bind.respBody(body.systemId)
      )
    }
  }
}
