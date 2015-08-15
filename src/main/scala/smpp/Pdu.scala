package smpp

import akka.util.{ByteIterator, ByteString, ByteStringBuilder}

case class Header(commandLength: Int, commandId: Int, commandStatus: Int, seqNumber: Int) {
  implicit val byteOrder = java.nio.ByteOrder.BIG_ENDIAN
  def toByteString = new ByteStringBuilder()
      .putInt(commandLength)
      .putInt(commandId)
      .putInt(commandStatus)
      .putInt(seqNumber).result()
}

trait Body {
  def toByteString: ByteString
}

trait Pdu {
  def header: Header
  def body: Body
  def toByteString: ByteString = header.toByteString ++ body.toByteString
}

object CommandId {
  val generic_nack          = 0x00000001
  val bind_receiver         = 0x00000001
  val bind_receiver_resp    = 0x80000001
  val bind_transmitter      = 0x00000002
  val bind_transmitter_resp = 0x80000002
  val query_sm              = 0x00000003
  val query_sm_resp         = 0x80000003
  val submit_sm             = 0x00000004
  val submit_sm_resp        = 0x80000004
  val deliver_sm            = 0x00000005
  val deliver_sm_resp       = 0x80000005
  val unbind                = 0x00000006
  val unbind_resp           = 0x80000006
  val replace_sm            = 0x00000007
  val replace_sm_resp       = 0x80000007
  val cancel_sm             = 0x00000008
  val cancel_sm_resp        = 0x80000008
  val bind_transceiver      = 0x00000009
  val bind_transceiver_resp = 0x80000009
  val out_bind              = 0x8000000B
  val enquire_link          = 0x00000015
  val enquire_link_resp     = 0x80000015
}

object Pdu {

  val SmppVersion: Byte = 0x34

  implicit val byteOrder = java.nio.ByteOrder.BIG_ENDIAN

  def respHeader(reqHeader: Header, commandStatus: Int, length: Int) =
    new Header(length, reqHeader.commandId | 0x80000000, commandStatus, reqHeader.seqNumber)

  def getHeader(iterator: ByteIterator): Header = {
    Header(iterator.getInt, iterator.getInt, iterator.getInt, iterator.getInt)
  }

  def getNullTermString(iterator: ByteIterator): String = {
    // Need to clone the iterator to get the first part.
    val iterator2 = iterator.clone()
    iterator2.takeWhile(_ > 0)
    // Clean up the provided iterator.
    iterator.dropWhile(_ > 0)
    iterator.getByte
    // Cloned iterator provides the string.
    iterator2.toByteString.utf8String
  }

  def parseRequest(data: ByteString): Pdu = {
    val iterator = data.iterator
    if (iterator.getInt != data.length)
      throw new IllegalArgumentException("Invalid length octet in PDU data")
    val header = getHeader(iterator)
    import CommandId._
    header.commandId match {
      case `bind_transmitter` => new BindTransmitter(header, Bind.getBody(iterator))
      case _ => throw new NotImplementedError(s"Unimplemented command ID $header.commandId")
    }
  }

  def nullTermString(string: String) = ByteString(string) ++ ByteString(0)
}