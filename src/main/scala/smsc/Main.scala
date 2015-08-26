package smsc

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

import scala.concurrent.duration._

/**
 * Main program for the SMSC Stub Server.
 *
 * Proper configuration will be coming soon.
 */
object Main extends App {

  import Tcp._

  /** Settings loaded from configuration files. */
  val settings = new Settings

  implicit val system = ActorSystem("smsc-stub-server")
  implicit val timeout = Timeout(5.seconds)
  
  val smppService = system.actorOf(Props[SmscStub], "smsc-stub")
  IO(Tcp) ! Bind(smppService, new InetSocketAddress("0.0.0.0", settings.smppPort))

  val httpService = system.actorOf(Props[SmscControl], "smsc-control")
  IO(Http) ? Http.Bind(httpService, interface = "0.0.0.0", port = settings.httpPort)

}