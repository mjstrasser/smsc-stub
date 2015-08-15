package smpp

import akka.util.ByteString

case class Header(commandLength: Int, commandId: Int, commandStatus: Int, seqNumber: Int)

trait Pdu

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
  def fromRequest(data: ByteString): Pdu = {

  }
}