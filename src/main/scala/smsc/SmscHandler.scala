package smsc

import akka.actor.{Actor, ActorLogging}
import akka.io.Tcp
import akka.util.ByteString

class SmscHandler extends Actor with ActorLogging {

  import Tcp._

  def handlePdu(data: ByteString) = ???

  def receive = {
    case Received(data) =>
      handlePdu(data)
    case PeerClosed =>
      log.info("Disconnected")
      context stop self
  }


}
