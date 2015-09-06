package smsc

/**
 * Simple model of short messages with a sender address, a recipient address
 * and message text.
 */
abstract class Message {
  def sender: String
  def recipient: String
  def message: String
}

/** Mobile-originated short message, sent from SMSC to ESME. */
case class MoMessage(sender: String, recipient: String, message: String) extends Message
/** Mobile-terminated short message, sent from ESME to SMSC. */
case class MtMessage(sender: String, recipient: String, message: String) extends Message
