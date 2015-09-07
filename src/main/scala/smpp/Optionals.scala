package smpp

/**
 * Optional parameters to PDUs: tags and constructed TLVs.
 *
 * Only those currently in use are defined.
 */
object Optionals {

  val receipted_message_id: Short       = 0x001E
  val user_message_reference: Short     = 0x0204
  val message_state: Short              = 0x0427

  val message_state_DELIVERED: Byte     = 2

  def receiptedMessageId(value: String) = TlvCString(receipted_message_id, value)
  def userMessageReference(ref: Short) = TlvShort(user_message_reference, ref)
  def messageStateDelivered = TlvByte(message_state, message_state_DELIVERED)

}
