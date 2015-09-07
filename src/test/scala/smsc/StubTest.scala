package smsc

import org.scalatest.FlatSpec
import smpp._

class StubTest extends FlatSpec {

  import CommandId._

  "Stub#responseTo" should "return bind_transmitter_resp to a bind_transmitter correctly" in {

    val request = new BindTransmitter(
      Header(bind_transmitter, 0, 1),
      BindBody("SYSTEM_ID", "password", "mess_gateway", Pdu.SmppVersion, 1, 1, "*")
    )
    val response = Seq(
      new BindTransmitterResp(
        Header(bind_transmitter_resp, 0, 1),
        BindRespBody(Stub.StubSystemId)
      )
    )

    assert(Stub.responsesTo(request) == response)

  }

}
