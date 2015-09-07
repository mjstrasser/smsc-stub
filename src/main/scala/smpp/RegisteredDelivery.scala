package smpp

/**
 * Definitions of the `registered_delivery` byte in message PDUs.
 * Reserved values are not shown.
 */
object RegisteredDelivery {

  // SMSC Delivery Receipts (bits 1 and 0)
  val smsc_no_receipt: Byte     = 0x00
  val smsc_rcpt_succ_fail: Byte = 0x01
  val smsc_rcpt_fail_only: Byte = 0x02

  // SME originated Acknowledgement (bits 3 and 2)
  val sme_no_receipt: Byte      = 0x00
  val sme_delivery_ack: Byte    = 0x04
  val sme_manual_ack: Byte      = 0x08
  val sme_del_man_ack: Byte     = 0x0C

  // Intermediate Notification (bit 5)
  val inter_no_notif: Byte      = 0x00
  val inter_notif_request: Byte = 0x10

  def smscReceiptRequested(regDelivery: Byte): Boolean = (regDelivery & smsc_rcpt_succ_fail) == smsc_rcpt_succ_fail

}
