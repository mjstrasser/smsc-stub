package smsc

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.Logging
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

  def receive = {

    case deliverSm: DeliverSm =>
      // DeliverSm object sent by the SmscControl actor.
      log.info("Sending: {}", deliverSm)
      val deliverBytes = deliverSm.toByteString
      log.debug("As bytes: {}", deliverBytes)
      logEndToEnd(deliverSm)
      randomReceiver ! Write(deliverBytes)

    case Received(data) =>
      // Data received from the SmscStub actor.
      log.debug("Received bytes: {}", data)
      val iterator = data.iterator
      while (iterator.hasNext)
        for (bytesResponse <- responseTo(iterator))
          sender ! Write(bytesResponse)

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
  def responseTo(iterator: ByteIterator): Seq[ByteString] = {

    val request = Pdu.parsePdu(iterator)
    log.info("Received: {}", request)
    logEndToEnd(request)

    // Request PDUs have the high bit unset.
    if ((request.header.commandId & 0x80000000) == 0) {
      Stub.responsesTo(request).map(responseToByteString)
    } else {
      // Got a PDU that does not require a response.
      Seq()
    }

  }

  /**
   * Converts a response PDU to an Akka `ByteString` after handling receiver bind
   * and unbind, and logging.
   *
   * @param response a response PDU
   * @return the PDU as bytes
   */
  private def responseToByteString(response: Pdu) = {

    log.info("Sending: {}", response)
    log.debug("As bytes: {}", response.toByteString)

    if (isReceiverBindResp(response))
      // Receiver or transceiver bind: add the sender to the receivers list.
      addReceiver(sender())
    else if (response.header.commandId == CommandId.unbind_resp)
      // Unbind: remove this sender from the receivers list.
      removeReceiver(sender())

    response.toByteString
  }

  /**
   * Returns true if the response PDU is a successful `bind_receiver_resp` or `bind_transceiver_resp`.
   */
  def isReceiverBindResp(response: Pdu) =
    response match {
      case BindReceiverResp(header, _) => header.commandStatus == CommandStatus.ESME_ROK
      case BindTransceiverResp(header, _) => header.commandStatus == CommandStatus.ESME_ROK
      case _ => false
    }

  /**
   * Second Akka logger, used only for end-to-end logging of DeliverSm and SubmitSm
   * messages for performance measurement purposes.
   *
   * Calling `Logging.getLogger` is necessary to create a logger with category "EndToEnd".
   */
  val e2eLog = Logging.getLogger(context.system, "EndToEnd")
  /**
   * Logs a [[DeliverSm]] or [[SubmitSm]] PDU to the end-to-end logger.
   * @param pdu the PDU to log
   */
  private def logEndToEnd(pdu: Pdu) = pdu match {
    case DeliverSm(header, body) => e2eLog.info("{},{},{}", body.source.addr, body.dest.addr, body.shortMessage)
    case SubmitSm(header, body) => e2eLog.info("{},{},{}", body.source.addr, body.dest.addr, body.shortMessage)
    case _ =>
  }

  /**
   * Adds an actor ref synchronously to the list.
   *
   * @param ref reference to the actor
   * @return count of receivers in the list after addition
   */
  def addReceiver(ref: ActorRef) = synchronized {
    log.debug(s"Adding receiver $ref")
    receivers += ref
  }

  /**
   * Removes an actor ref synchronously from the list.
   *
   * @param ref reference to the actor
   * @return count of receivers in the list after removal
   */
  def removeReceiver(ref: ActorRef) = synchronized {
    log.debug(s"Removing receiver $ref")
    receivers -= ref
  }

  /**
   * Randomly selects a receiver synchronously from the list.
   * @return one of the receivers in the list
   */
  def randomReceiver: ActorRef = synchronized {
    if (receivers.isEmpty)
      throw new IllegalStateException("No receivers have been bound")
    val receiver = receivers(Random.nextInt(receivers.size))
    log.debug(s"Retrieved receiver $receiver")
    receiver
  }

}

object SmscHandler {
  /**
   * A synchronous list of actors for bound SMPP receiver or transceiver
   * connections.
   */
  private val receivers = ListBuffer[ActorRef]()

}
