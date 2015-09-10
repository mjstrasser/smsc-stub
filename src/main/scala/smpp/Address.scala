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
  def toByteString = ByteString(ton, npi) ++ cString(addr)
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

  def parseAddress(iter: ByteIterator) = new Address(iter.getByte, iter.getByte, parseCString(iter))

  /**
   * Constructs an Address object with TON and NPI to match the specified address string.
   *
   * - For Australian MSISDNs they are `ton_international` and `npi_ISDN`
   * - For International MSISDNs they are `ton_international` and `npi_ISDN`
   * - For short codes (e.g. 1776) they are `ton_national` and `npi_ISDN`
   * - For alphabetic strings (e.g. TelstraData) they are `ton_alphanumeric` and `npi_unknown`
   *
   * @param addr an address string
   * @return the appropriate Address object
   */
  def apply(addr: String) = {
    val AU_MSISDN = "^\\+?614\\d{8}$".r
    val INTL_MSISDN = "^\\+\\d+$".r
    val AU_SHORT = "^1\\d+$".r
    val ALPHA = "^[^\\d]+$".r
    val (ton, npi) = addr match {
      case AU_MSISDN() => (ton_international, npi_ISDN)
      case INTL_MSISDN() => (ton_international, npi_ISDN)
      case AU_SHORT() => (ton_national, npi_ISDN)
      case ALPHA() => (ton_alphanumeric, npi_unknown)
      case _ => (ton_unknown, npi_unknown)
    }
    new Address(ton, npi, addr)
  }
}