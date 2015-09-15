package smsc

import java.util.concurrent.{ConcurrentHashMap, CopyOnWriteArrayList}

import akka.actor.{Actor, ActorRef, DiagnosticActorLogging}
import akka.event.Logging
import akka.io.Tcp
import akka.util.{ByteIterator, ByteString}
import smpp._

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
class SmscHandler extends Actor with DiagnosticActorLogging {

  import SmscHandler._
  import Tcp._

  def receive = {

    case deliverSm: DeliverSm =>
      // DeliverSm object sent by the SmscControl actor.
      val esme = randomReceiver
      log.mdc(systemIdMdc(esme))
      log.info("Sending: {}", deliverSm)
      val deliverBytes = deliverSm.toByteString
      log.debug("As bytes: {}", deliverBytes)
      logEndToEnd(deliverSm)
      esme ! Write(deliverBytes)

    case Received(data) =>
      // Data received from the SmscStub actor.
      val esme = sender()
      log.mdc(systemIdMdc(esme))
      log.debug("Received bytes: {}", data)
      val iterator = data.iterator
      while (iterator.hasNext)
        for (bytesResponse <- responseTo(iterator))
          esme ! Write(bytesResponse)
      log.clearMDC()

    case PeerClosed =>
      // TCP connection closed.
      log.debug("Disconnected")
      context stop self
  }

  def systemIdMdc(esme: ActorRef) = {
    val systemId = if (esmeBinds.containsKey(esme)) esmeBinds.get(esme) else ""
    Map("systemId" -> systemId)
  }

  /**
   * Manages bind and unbind request PDUs by storing references to the calling
   * actor in a map for logging MDC and in a list (buffer) for receiving actors
   * to use when sending [[DeliverSm]] PDUs.
   *
   * @param request the incoming request PDU
   */
  def manageBinds(request: Pdu) = {
    val esme = sender()
    request match {
      case BindTransmitter(_, body) =>
        esmeBinds.put(esme, s"${body.systemId}/TX")
      case BindTransceiver(_, body) =>
        esmeBinds.put(esme, s"${body.systemId}/TRX")
        addReceiver(esme)
      case BindReceiver(_, body) =>
        esmeBinds.put(esme, s"${body.systemId}/RX")
        addReceiver(esme)
      case unb: Unbind =>
        esmeBinds.remove(esme)
      case _ =>
    }
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

    manageBinds(request)

    // Request PDUs have the high bit unset.
    if ((request.header.commandId & 0x80000000) == 0) {
      Stub.responsesTo(request).map(responseToByteString)
    } else {
      // Got a PDU that does not require a response.
      Seq()
    }

  }

  /**
   * Converts a response PDU to an Akka `ByteString` after logging.
   *
   * @param response a response PDU
   * @return the PDU as bytes
   */
  private def responseToByteString(response: Pdu) = {
    log.info("Sending: {}", response)
    log.debug("As bytes: {}", response.toByteString)
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
   * Adds an actor ref to the list.
   *
   * @param ref reference to the actor
   */
  def addReceiver(ref: ActorRef) = {
    receivers.add(ref)
    log.debug("Added receiver {}, count = {}", ref, receivers.size)
  }

  /**
   * Removes an actor ref from the list.
   *
   * @param ref reference to the actor
   */
  def removeReceiver(ref: ActorRef) = {
    receivers.remove(ref)
    log.debug("Removed receiver {}, count = {}", ref, receivers.size)
  }

  /**
   * Randomly selects a receiver from the list.
   *
   * @return one of the receivers in the list
   */
  def randomReceiver: ActorRef = {
    if (receivers.isEmpty)
      throw new IllegalStateException("No receivers have been bound")
    val receiver = receivers.get(Random.nextInt(receivers.size))
    log.debug("Retrieved receiver {}", receiver)
    receiver
  }

}

object SmscHandler {
  /**
   * A synchronised list of actors for bound SMPP receiver or transceiver
   * connections.
   */
  private val receivers = new CopyOnWriteArrayList[ActorRef]

  /**
   * A synchronised map of bound ESMEs keyed by actor (ref) and containing System ID strings.
   */
  private val esmeBinds = new ConcurrentHashMap[ActorRef, String]
}
