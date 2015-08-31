package smpp

import akka.util.ByteString
import org.scalatest.FlatSpec

class PduTest extends FlatSpec {

  import smpp.CommandId._

  val bindTxBytes = ByteString(
    // Header: length = 16
    0, 0, 0, 53,      // command_length (16 + 37 = 53)
    0, 0, 0,  2,      // command_id
    0, 0, 0,  0,      // command_status = NULL
    0, 0, 0,  1       // sequence_number
  ) ++
  // Body: length = 37
  ByteString('S', 'Y', 'S', 'T', 'E', 'M', '_', 'I', 'D', 0) ++                 // system_id
  ByteString('p', 'a', 's', 's', 'w', 'o', 'r', 'd', 0) ++                      // password
  ByteString('m', 'e', 's', 's', '_', 'g', 'a', 't', 'e', 'w', 'a', 'y', 0) ++  // system_type
  ByteString(0x34,                // interface_version
  1,                              // addr_ton
  1) ++                           // addr_npi
  ByteString('*', 0)              // address_range (2)

  val bindTxPdu = BindTransmitter(
      Header(bind_transmitter, 0, 1),
      BindBody("SYSTEM_ID", "password", "mess_gateway", Pdu.SmppVersion, 1, 1, "*")
    )

  "Pdu#toByteString" should "correctly construct a ByteString" in {
    assert(bindTxPdu.toByteString == bindTxBytes)
  }

  "Pdu#parseRequest" should "correctly parse a ByteString" in {
    assert(Pdu.parsePdu(bindTxBytes.iterator) == bindTxPdu)
  }

}
