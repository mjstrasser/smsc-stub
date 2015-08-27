package smsc

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp
import akka.util.{ByteIterator, ByteString}
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

  import SmscHandler._
  import Tcp._

  val endToEndLogger = context.actorOf(Props[EndToEndLogger])

  def receive = {

    case deliverSm: DeliverSm =>
      // DeliverSm object sent by the SmscControl actor.
      log.info("Sending: {}", deliverSm)
      val deliverBytes = deliverSm.toByteString
      log.debug("As bytes: {}", deliverBytes)
      endToEndLogger ! deliverSm
      randomReceiver ! Write(deliverBytes)

    case Received(data) =>
      // Data received from the SmscStub actor.
      log.debug("Received bytes: {}", data)
      val iterator = data.iterator
      while (iterator.hasNext)
        sender ! Write(responseTo(iterator))

    case PeerClosed =>
      // TCP connection closed.
      log.debug("Disconnected")
      context stop self
  }

  /**
   * Generate a response to a PDU received over TCP (via [[SmscStub]]).
   *
   * @param iterator an iterator with the bytes
   * @return a PDU as bytes
   */
  def responseTo(iterator: ByteIterator): ByteString = {

    val request = Pdu.parsePdu(iterator)
    log.info("Received: {}", request)
    endToEndLogger ! request

    if ((request.header.commandId & 0x80000000) == 0) {

      // Request PDUs have the high bit unset.
      val response = Stub.responseTo(request)
      log.info("Sending: {}", response)
      log.debug("As bytes: {}", response.toByteString)

      if (isReceiverBindResp(response))
        // Receiver or transceiver bind: add the sender to the receivers list.
        addReceiver(sender())
      else if (response.header.commandId == CommandId.unbind_resp)
        // Unbind: remove this sender from the receivers list.
        removeReceiver(sender())

      response.toByteString
    } else {
      // Got a PDU that does not require a response.
      ByteString()
    }

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
  private val receivers = ListBuffer[ActorRef]()

  /**
   * Adds an actor ref synchronously to the list.
   *
   * @param ref reference to the actor
   * @return count of receivers in the list after addition
   */
  def addReceiver(ref: ActorRef) = synchronized {
    receivers += ref
    receivers.length
  }

  /**
   * Removes an actor ref synchronously from the list.
   *
   * @param ref reference to the actor
   * @return count of receivers in the list after removal
   */
  def removeReceiver(ref: ActorRef) = synchronized {
    receivers -= ref
    receivers.length
  }

  /**
   * Randomly selects a receiver synchronously from the list.
   * @return one of the receivers in the list
   */
  def randomReceiver = synchronized {
    val length = receivers.length
    if (length < 1)
      throw new IllegalStateException("No receivers have been bound")
    receivers(Random.nextInt(length))
  }
}
