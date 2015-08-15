package smsc

import smpp._

object Stub {
  def response(request: Pdu) = {
    request match {
      case BindTransmitter(header, body) => new BindTransmitterResp(
        Pdu.respHeader(header, 0, 17 + body.systemId.length),
        Bind.respBody(body.systemId)
      )
    }
  }
}
