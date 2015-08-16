package smsc

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp
import akka.util.ByteString
import smpp._

import scala.collection.mutable.ListBuffer
import scala.util.Random

class SmscHandler extends Actor with ActorLogging {

  import Tcp._

  def responseTo(data: ByteString): ByteString = {
    val request = Pdu.parseRequest(data)
    log.info("Request: {}", request)
    val response = Stub.responseTo(request)
    log.info("Response: {}", response)

    if (isReceiverBindResp(response))
      SmscHandler.receivers += sender
    else if (response.header.commandId == CommandId.unbind_resp)
      SmscHandler.receivers -= sender

    response.toByteString
  }

  def receive = {
    case Received(data) =>
      log.info("Received data: {}", data)
      sender ! Write(responseTo(data))
    case PeerClosed =>
      log.info("Disconnected")
      context stop self
  }

//  def sendDeliverSm(deliverSm: DeliverSm): DeliverSmResp {
//    randomSender ! Write(deliverSm.toBytes)
//  }

  def randomSender = {
    val length = SmscHandler.receivers.length
    if (length < 1)
      throw new IllegalStateException("No receivers have been bound")
    SmscHandler.receivers(Random.nextInt(length))
  }

  def isReceiverBindResp(response: Pdu) =
    response match {
      case BindReceiverResp(header, _) => header.commandStatus == CommandStatus.ESME_ROK
      case BindTransceiverResp(header, _) => header.commandStatus == CommandStatus.ESME_ROK
      case _ => false
    }

}

object SmscHandler {

  val receivers = ListBuffer[ActorRef]()

}
