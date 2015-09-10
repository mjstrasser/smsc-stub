package smpp

/**
 * Optional parameters to PDUs: tags and constructed TLVs.
 */
object Optionals {

  val dest_addr_subunit: Short            = 0x0005
  val dest_network_type: Short            = 0x0006
  val dest_bearer_type: Short             = 0x0007
  val dest_telematics_id: Short           = 0x0008
  val source_addr_subunit: Short          = 0x000D
  val source_network_type: Short          = 0x000E
  val source_bearer_type: Short           = 0x000F
  val source_telematics_id: Short         = 0x0010
  val qos_time_to_live: Short             = 0x0017
  val payload_type: Short                 = 0x0019
  val additional_status_info_text: Short  = 0x001D
  val receipted_message_id: Short         = 0x001E
  val ms_msg_wait_facilities: Short       = 0x0030
  val privacy_indicator: Short            = 0x0201
  val source_subaddress: Short            = 0x0202
  val dest_subaddress: Short              = 0x0203
  val user_message_reference: Short       = 0x0204
  val user_response_code: Short           = 0x0205
  val source_port: Short                  = 0x020A
  val destination_port: Short             = 0x020B
  val sar_msg_ref_num: Short              = 0x020C
  val language_indicator: Short           = 0x020D
  val sar_total_segments: Short           = 0x020E
  val sar_segment_seqnum: Short           = 0x020F
  val SC_interface_version: Short         = 0x0210
  val callback_num_pres_ind: Short        = 0x0302
  val callback_num_atag: Short            = 0x0303
  val number_of_messages: Short           = 0x0304
  val callback_num: Short                 = 0x0381
  val dpf_result: Short                   = 0x0420
  val set_dpf: Short                      = 0x0421
  val ms_availability_status: Short       = 0x0422
  val network_error_code: Short           = 0x0423
  val message_payload: Short              = 0x0424
  val delivery_failure_reason: Short      = 0x0425
  val more_messages_to_send: Short        = 0x0426
  val message_state: Short                = 0x0427
  val ussd_service_op: Short              = 0x0501
  val display_time: Short                 = 0x1201
  val sms_signal: Short                   = 0x1203
  val ms_validity: Short                  = 0x1204
  val alert_on_message_delivery: Short    = 0x130C
  val its_reply_type: Short               = 0x1380
  val its_session_info: Short             = 0x1383

  // Some values.
  val message_state_DELIVERED: Byte     = 2

  def receiptedMessageId(value: String) = TlvCString(receipted_message_id, value)
  def userMessageReference(ref: Short) = TlvShort(user_message_reference, ref)
  def messageStateDelivered = TlvByte(message_state, message_state_DELIVERED)

}
