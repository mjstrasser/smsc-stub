package smsc

import akka.actor.{Actor, ActorLogging}
import akka.io.Tcp
import akka.util.ByteString
import smpp.Pdu

class SmscHandler extends Actor with ActorLogging {

  import Tcp._

  def responseTo(data: ByteString): ByteString = {
    val request = Pdu.parseRequest(data)
    val response = Stub.response(request)
    response.toByteString
  }

  def receive = {
    case Received(data) =>
      sender ! Write(responseTo(data))
    case PeerClosed =>
      log.info("Disconnected")
      context stop self
  }


}
