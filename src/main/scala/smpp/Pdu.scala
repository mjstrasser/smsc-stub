package smpp

import akka.util.{ByteIterator, ByteString, ByteStringBuilder}

case class Header(commandId: Int, commandStatus: Int, seqNumber: Int) {
  implicit val byteOrder = java.nio.ByteOrder.BIG_ENDIAN
  def toByteString(commandLength: Int) = new ByteStringBuilder()
      .putInt(commandLength)
      .putInt(commandId)
      .putInt(commandStatus)
      .putInt(seqNumber).result()
  val length = 16
}

trait Body {
  def toByteString: ByteString
}

trait Pdu {
  def header: Header
  def body: Body
  def toByteString: ByteString = {
    val bodyByteString = body.toByteString
    header.toByteString(header.length + bodyByteString.length) ++ bodyByteString
  }
}

case class EmptyBody() extends Body {
  def toByteString = ByteString()
}

case class GenericNack(header: Header, body: EmptyBody) extends Pdu
case class NoPdu(header: Header, body: EmptyBody) extends Pdu {
    override def toByteString = ByteString()
}

object Pdu {

  val SmppVersion: Byte = 0x34

  implicit val byteOrder = java.nio.ByteOrder.BIG_ENDIAN

  def respHeader(reqHeader: Header, commandStatus: Int) =
    Header(reqHeader.commandId | 0x80000000, commandStatus, reqHeader.seqNumber)

  def getHeader(iterator: ByteIterator): Header = {
    Header(iterator.getInt, iterator.getInt, iterator.getInt)
  }

  def getNullTermString(iterator: ByteIterator): String = {
    // Need to clone the iterator to get the first part.
    val iterator2 = iterator.clone()
    iterator2.takeWhile(_ > 0)
    // Clean up from the provided iterator.
    iterator.dropWhile(_ > 0)
    // Remove the terminating null unless at the end of the ByteString.
    if (iterator.nonEmpty) iterator.getByte
    // Cloned iterator provides the string.
    iterator2.toByteString.utf8String
  }

  def parseRequest(data: ByteString): Pdu = {
    val iterator = data.iterator
    val commandLength = iterator.getInt
    if (commandLength != data.length)
      throw new IllegalArgumentException("Invalid length octet in PDU data")
    val header = getHeader(iterator)
    import CommandId._
    header.commandId match {
      case `bind_transmitter` => BindTransmitter(header, Bind.getBody(iterator))
      case `bind_receiver` => BindReceiver(header, Bind.getBody(iterator))
      case `bind_transceiver` => BindTransceiver(header, Bind.getBody(iterator))
      case `unbind` => Unbind(header, EmptyBody())
      case `enquire_link` => EnquireLink(header, EmptyBody())
      case `submit_sm` => SubmitSm(header, Submit.getBody(iterator))
      case `generic_nack` => GenericNack(header, EmptyBody())
      case _ => throw new NotImplementedError(s"Unimplemented command ID ${header.commandId}")
    }
  }

  def nullTermString(string: String) = ByteString(string, "ASCII") ++ ByteString(0)
}