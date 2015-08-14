package smpp

case class Header(commandLength: Int, commandId: Int, commandStatus: Int, seqNumber: Int)

trait Pdu
