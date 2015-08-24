package smsc

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp
import akka.util.ByteString
import smpp._

import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
 * An actor that handles SMPP requests, behaving as an SMSC stub.
 *
 * It receives:
 *
 *  - SMPP requests from an ESME as ByteString objects from [[SmscStub]] actor.
 *  - [[SmscStub]] objects from [[SmscControl]] actor to send to a bound receiver
 *    or transceiver ESME.
 */
class SmscHandler extends Actor with ActorLogging {

  import Tcp._

  def receive = {

    case deliverSm: DeliverSm =>
      // DeliverSm object sent by the SmscControl actor.
      log.info("Received DeliverSm: {}", deliverSm)
      randomReceiver ! Write(deliverSm.toByteString)

    case Received(data) =>
      // Data received from the SmscStub actor.
      log.info("Received data: {}", data)
      sender ! Write(responseTo(data))

    case PeerClosed =>
      // TCP connection closed.
      log.info("Disconnected")
      context stop self
  }

  /**
   * Generate a response to a PDU received over TCP (via [[SmscStub]]).
   *
   * @param data the PDU as bytes
   * @return a PDU as bytes
   */
  def responseTo(data: ByteString): ByteString = {

    val request = Pdu.parseRequest(data)
    log.info("Request: {}", request)

    val response = Stub.responseTo(request)
    log.info("Response: {}", response)

    if (isReceiverBindResp(response))
      // Receiver or transceiver bind: add the sender to the receivers list.
      SmscHandler.receivers += sender
    else if (response.header.commandId == CommandId.unbind_resp)
      // Unbind: remove this sender from the receivers list.
      SmscHandler.receivers -= sender

    response.toByteString
  }

  /**
   * Randomly select an SMPP receiver from the current list.
   *
   * @return one of the receivers in the list
   */
  def randomReceiver = {
    val length = SmscHandler.receivers.length
    if (length < 1)
      throw new IllegalStateException("No receivers have been bound")
    SmscHandler.receivers(Random.nextInt(length))
  }

  /**
   * Return true if the response PDU is a successful `bind_receiver_resp` or `bind_transceiver_resp`.
   */
  def isReceiverBindResp(response: Pdu) =
    response match {
      case BindReceiverResp(header, _) => header.commandStatus == CommandStatus.ESME_ROK
      case BindTransceiverResp(header, _) => header.commandStatus == CommandStatus.ESME_ROK
      case _ => false
    }

}

object SmscHandler {
  /**
   * A variable list of actors for bound SMPP receiver or transceiver
   * connections.
   */
  val receivers = ListBuffer[ActorRef]()
}
