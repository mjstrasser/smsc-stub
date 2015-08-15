package smsc

import akka.actor.{Actor, ActorLogging}
import akka.io.Tcp
import akka.util.ByteString
import smpp.{BindTransmitter, Pdu}

class SmscHandler extends Actor with ActorLogging {

  import Tcp._

  def responseTo(data: ByteString): ByteString = {
    Pdu.parseRequest(data) match {
//      case BindTransmitter => ???
      case _ => ???
    }
  }

  def receive = {
    case Received(data) =>
      sender ! Write(responseTo(data))
    case PeerClosed =>
      log.info("Disconnected")
      context stop self
  }


}
