package smsc

import smpp._

object Stub {
  def responseTo(request: Pdu) = {
    request match {
      case BindTransmitter(header, body) => new BindTransmitterResp(
        Pdu.respHeader(header, 0, 17 + body.systemId.length),
        Bind.respBody(body.systemId)
      )
    }
  }
}
