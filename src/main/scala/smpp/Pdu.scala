package smpp

import akka.util.{ByteIterator, ByteString, ByteStringBuilder}
import org.slf4j.LoggerFactory

/**
 * Header of SMPP PDUs excluding the `command_length`, which is calculated when
 * the PDU is converted to a ByteString to be sent.
 *
 * @param commandId identifier of the PDU with values from [[CommandId]]
 * @param commandStatus status of the command with values from [[CommandStatus]]
 * @param seqNumber sequence of the request used to correlate with a response
 */
case class Header(commandId: Int, commandStatus: Int, seqNumber: Int) {

  /** SMPP specifies big-endian byte order. */
  implicit val byteOrder = java.nio.ByteOrder.BIG_ENDIAN

  /**
   * Writes the header to a ByteString with the specified value for `command_length`.
   *
   * @param bodyLength length of the PDU body
   * @return a ByteString with the header
   */
  def toByteString(bodyLength: Int) = new ByteStringBuilder()
    .putInt(length + bodyLength)
    .putInt(commandId)
    .putInt(commandStatus)
    .putInt(seqNumber)
    .result()
  /** SMPP PDU headers are all 16 bytes long (I think) */
  // TODO: Confirm that all PDU headers are 16 bytes long.
  val length = 16

  override def toString = f"(id: $commandId%08X status: $commandStatus%d seq: $seqNumber%d)"
}

/**
 * The body of an SMPP PDU. It can be empty or contain contents that must be
 * written to bytes.
 */
trait Body {
  def toByteString: ByteString
}

/**
 * An SMPP protocol data unit (PDU). Every PDU has a header and optionally a body.
 */
trait Pdu {
  val name: String
  def header: Header
  def body: Body

  /**
   * The network representation of an SMPP PDU with header followed by body.
   * @return the combination of header and body
   */
  def toByteString: ByteString = {
    val bodyByteString = body.toByteString
    header.toByteString(bodyByteString.length) ++ bodyByteString
  }

  override def toString = s"($name: $header $body)"
}

/**
 * Zero-byte body for PDUs that need it (e.g. [[Unbind]] and [[GenericNack]]).
 */
case class EmptyBody() extends Body {
  def toByteString = ByteString()
  override def toString = ""
}

/** Class for the SMPP `generic_nack` PDU. */
case class GenericNack(header: Header, body: EmptyBody) extends Pdu {
  val name = "nack"
  override def toString = "(nack)"
}

/** Fake PDU for when no reply is required: only to [[smpp.GenericNack]]. */
case class NoPdu(header: Header, body: EmptyBody) extends Pdu {
  val name = "NO PDU!"
  override def toByteString = ByteString()
  override def toString = "(none)"
}

/** Object with utilities for constructing and parsing generic PDU elements. */
object Pdu {

  final val log = LoggerFactory.getLogger(Pdu.getClass)

  /** Only version 3.4 of SMPP is supported. */
  val SmppVersion: Byte = 0x34

  /** SMPP specifies big-endian byte order. */
  implicit val byteOrder = java.nio.ByteOrder.BIG_ENDIAN

  /**
   * Constructs a response header to match a request header.
   *
   * The `command_id` for a response sets bit 31 of the value from the request (see [[CommandId]]).
   *
   * @param reqHeader header from a request
   * @param commandStatus status of the response with a value from [[CommandStatus]]
   * @return the header to send in the response
   */
  def respHeader(reqHeader: Header, commandStatus: Int) =
    Header(reqHeader.commandId | 0x80000000, commandStatus, reqHeader.seqNumber)

  /**
   * Parses the header from a ByteString after `command_length` has been read from
   * the first four bytes.
   *
   * @param iterator the iterator used to read the input data
   * @return a header
   */
  def parseHeader(iterator: ByteIterator): Header = {
    Header(iterator.getInt, iterator.getInt, iterator.getInt)
  }

  /**
   * Parses a null-terminated C-string from a ByteString.
   *
   * @param iterator the iterator used to read the input data
   * @return the string in the input data
   */
  def parseNullTermString(iterator: ByteIterator): String = {
    // Need to clone the iterator to get the first part.
    val iterator2 = iterator.clone()
    iterator2.takeWhile(_ > 0)
    // Clean up from the provided iterator.
    iterator.dropWhile(_ > 0)
    // Remove the terminating null unless at the end of the ByteString.
    if (iterator.nonEmpty) iterator.getByte
    // Cloned iterator provides the string.
    // TODO: Ensure the string is actually ASCII.
    iterator2.toByteString.utf8String
  }

  def parseOctetString(iterator: ByteIterator, length: Int): String = {
    // Need to clone the iterator to get the first part.
    val iterator2 = iterator.clone()
    iterator2.take(length)
    // Clean up from the provided iterator.
    iterator.drop(length)
    // Cloned iterator provides the string.
    // TODO: Ensure the string is actually ASCII.
    iterator2.toByteString.utf8String
  }

  /**
   * Parses one SMPP PDUs from data bytes.
   *
   * These PDUs are currently known:
   *
   *  - [[BindTransmitter]]
   *  - [[BindTransceiver]]
   *  - [[BindReceiver]]
   *  - [[Unbind]]
   *  - [[EnquireLink]]
   *  - [[SubmitSm]]
   *  - [[DeliverSmResp]]
   *  - [[GenericNack]]
   *
   * @param iterator over the bytes to parse
   * @return the SMPP PDU
   */
  def parsePdu(iterator: ByteIterator): Pdu = {

    val commandLength = iterator.getInt
    log.debug("Command length: {}", commandLength)
    val header = parseHeader(iterator)
    import CommandId._
    header.commandId match {
      case `bind_transmitter` => BindTransmitter(header, Bind.parseBody(iterator))
      case `bind_receiver` => BindReceiver(header, Bind.parseBody(iterator))
      case `bind_transceiver` => BindTransceiver(header, Bind.parseBody(iterator))
      case `unbind` => Unbind(header, EmptyBody())
      case `enquire_link` => EnquireLink(header, EmptyBody())
      case `submit_sm` => SubmitSm(header, Submit.parseBody(iterator))
      case `deliver_sm_resp` => DeliverSmResp(header, Deliver.parseRespBody(iterator))
      case `cancel_sm` => CancelSm(header, Cancel.parseBody(iterator))
      case `generic_nack` => GenericNack(header, EmptyBody())
      case _ => throw new IllegalArgumentException(f"Unimplemented command ID ${header.commandId}%08X")
    }
  }

  /**
   * Constructs a null-terminated C-string with ASCII encoding.
   *
   * @param string source string
   * @return bytes of the string with a null byte appended
   */
  def nullTermString(string: String) = ByteString(string, "ASCII") ++ ByteString(0)

  /**
   * Constructs an octet string with ASCII encoding.
   *
   * @param string source string
   * @return bytes of the string in ASCII
   */
  def octetString(string: String) = ByteString(string, "ASCII")
}