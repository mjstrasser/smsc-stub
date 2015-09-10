package smpp

import org.scalatest.FlatSpec

class CancelTest extends FlatSpec {

  "Cancel" should "do something" in {
    val header = Header(CommandId.cancel_sm, CommandStatus.NULL, 123345)
    val source = Address("1776")
    val dest = Address("61402002002")
    val body = CancelBody("", "Stub00001", source, dest)
    val cancel = CancelSm(header, body)

    val bodyBytes = body.toByteString
    val cancelBytes = header.toByteString(bodyBytes.length) ++ bodyBytes

    assert(cancel.toByteString == cancelBytes)

  }

}
