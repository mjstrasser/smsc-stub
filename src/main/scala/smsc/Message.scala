package smsc

abstract class Message {
  def sender: String
  def recipient: String
  def message: String
}

case class MoMessage(sender: String, recipient: String, message: String) extends Message
case class MtMessage(sender: String, recipient: String, message: String) extends Message
