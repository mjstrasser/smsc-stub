package smsc

import java.util.concurrent.atomic.AtomicLong

import smpp._
import Pdu._
import CommandStatus._

object Stub {
  def responseTo(request: Pdu) = {
    request match {
      case BindTransmitter(header, body) => BindTransmitterResp(respHeader(header, NULL), Bind.respBody(body.systemId))
      case BindReceiver(header, body) => BindReceiverResp(respHeader(header, NULL), Bind.respBody(body.systemId))
      case BindTransceiver(header, body) => BindTransceiverResp(respHeader(header, NULL), Bind.respBody(body.systemId))
      case Unbind(header, body) => Unbind(respHeader(header, NULL), body)
      case EnquireLink(header, body) => EnquireLinkResp(respHeader(header, ESME_ROK), body)
      case SubmitSm(header, body) => SubmitSmResp(respHeader(header, ESME_ROK), Submit.respBody(newMessageId))
    }
  }

  val counter: AtomicLong = new AtomicLong(0)
  def newMessageId = "Smsc%08d".format(counter.incrementAndGet)
}
