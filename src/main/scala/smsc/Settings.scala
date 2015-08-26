package smsc

import com.typesafe.config.ConfigFactory

class Settings {

  val config = ConfigFactory.load()

  val smppPort = config.getInt("smpp-port")
  val httpPort = config.getInt("http-port")

}
