package smpp

/**
 * Definitions of the `esm_class` byte in message PDUs.
 * Reserved values are not shown.
 */
object EsmClass {

  // Encodings in ESME -> SMSC PDUs.
  // Messaging Mode (bits 1-0)
  val default_smsc_mode: Byte         = 0x00
  val datagram_mode: Byte             = 0x01
  val forward_mode: Byte              = 0x02
  val store_and_forward_mode: Byte    = 0x03

  // Message Type (bits 5-2)
  val default_message_type: Byte      = 0x00
  val esme_delivery_ack: Byte         = 0x08
  val esme_manual_ack: Byte           = 0x10

  // GSM Network Specific Features (bits 7-6)
  // TODO if needed.

  // Encodings in SMSC -> ESME PDUs.
  // Bits 1 and 0 are ignored
  // Message Type (bits 5-2)
  val smsc_delivery_rcpt: Byte        = 0x04
  val sme_delivery_ack: Byte          = 0x08
  val sme_manual_ack: Byte            = 0x10
  val conversation_abort: Byte        = 0x18
  val inter_delivery_notif: Byte      = 0x20

}
