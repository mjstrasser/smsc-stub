package smpp

import akka.util.ByteString
import org.scalatest.FunSuite

class PduTest extends FunSuite {

  val bindTx = ByteString(
    // Header: length = 16
    0, 0, 0, 53,      // command_length (16 + 37 = 53)
    0, 0, 0,  2,      // command_id
    0, 0, 0,  0,      // command_status = NULL
    0, 0, 0,  1       // sequence_number
  ) ++
  // Body: length = 37
  ByteString("SYSTEM_ID\0") ++    // system_id (10)
  ByteString("password\0") ++     // password (9)
  ByteString("mess_gateway\0") ++ // system_type (13)
  ByteString(0x34,                // interface_version
  1,                              // addr_ton
  1) ++                           // addr_npi
  ByteString("*\0")               // address_range (2)

}
