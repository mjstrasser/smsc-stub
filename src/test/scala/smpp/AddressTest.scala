package smpp

import akka.util.ByteString
import org.scalatest.FlatSpec

class AddressTest extends FlatSpec {

  import Address._

  "Address#apply(String)" should "construct Address objects for MSISDNs with ton_international and npi_isdn" in {
    var address = Address("61401001001")
    assert(address.ton == ton_international && address.npi == npi_ISDN)
    address = Address("+61422334455")
    assert(address.ton == ton_international && address.npi == npi_ISDN)
    address = Address("+442314773423")
    assert(address.ton == ton_international && address.npi == npi_ISDN)
  }

  it should "construct Address objects for shortcodes with ton_national and npi_isdn" in {
    var address = Address("1776")
    assert(address.ton == ton_national && address.npi == npi_ISDN)
    address = Address("19702300")
    assert(address.ton == ton_national && address.npi == npi_ISDN)
  }

  it should "construct Address objects for alphabetic addresses with ton_alphanumeric and npi_unknown" in {
    var address = Address("TelstraData")
    assert(address.ton == ton_alphanumeric && address.npi == npi_unknown)
    address = Address("Skip")
    assert(address.ton == ton_alphanumeric && address.npi == npi_unknown)
  }

  it should "construct Address objects with ton_unknown and npi_unknown for all other address strings" in {
    val address = Address("Jump1234")
    assert(address.ton == ton_unknown && address.npi == npi_unknown)
  }

  "Address#toByteString" should "correctly encode an address" in {
    val address = Address("61401001001")
    val addressBytes = ByteString(ton_international, npi_ISDN) ++
      ByteString("61401001001") ++ ByteString(0)
    assert(address.toByteString == addressBytes)
  }

}
