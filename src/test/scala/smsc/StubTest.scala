package smsc

import org.scalatest.FlatSpec
import smpp._

class StubTest extends FlatSpec {

  import CommandId._

  "Stub#responseTo" should "return bind_transmitter_resp to a bind_transmitter correctly" in {

    val request = new BindTransmitter(
      new Header(53, bind_transmitter, 0, 1),
      new BindBody("SYSTEM_ID", "password", "mess_gateway", Pdu.SmppVersion, 1, 1, "*")
    )
    val response = new BindTransmitterResp(
      new Header(26, bind_transmitter_resp, 0, 1),
      new BindRespBody("SYSTEM_ID")
    )

    assert(Stub.responseTo(request) == response)

  }

}
