package smpp

import akka.util.{ByteIterator, ByteString}
import smpp.Pdu._

/**
 * SMPP source or destination address, comprising TON, NPI and address.
 * @param ton type of number
 * @param npi numbering plan indicator
 * @param addr address string
 */
case class Address(ton: Byte, npi: Byte, addr: String) {
  override def toString = s"($ton $npi $addr)"
  def toByteString = ByteString(ton, npi) ++ nullTermString(addr)
}

object Address {

  val ton_unknown: Byte           = 0x00
  val ton_international: Byte     = 0x01
  val ton_national: Byte          = 0x02
  val ton_network_specific: Byte  = 0x03
  val ton_subscriber_number: Byte = 0x04
  val ton_alphanumeric: Byte      = 0x05
  val ton_abbreviated: Byte       = 0x06

  val npi_unknown: Byte           = 0x00
  val npi_ISDN: Byte              = 0x01
  val npi_data: Byte              = 0x03
  val npi_telex: Byte             = 0x04
  val npi_land_mobile: Byte       = 0x06
  val npi_national: Byte          = 0x08
  val npi_private: Byte           = 0x09
  val npi_ERMES: Byte             = 0x0A
  val npi_internet: Byte          = 0x0E
  val npi_wap_client_id: Byte     = 0x12

  def parseAddress(iter: ByteIterator) = new Address(iter.getByte, iter.getByte, parseNullTermString(iter))
}