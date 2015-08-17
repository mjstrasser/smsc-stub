package smsc

import akka.actor.{ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.pattern.ask
import akka.util.Timeout
import java.net.InetSocketAddress
import scala.concurrent.duration._
import spray.can.Http

object Main extends App {

  import Tcp._

  implicit val system = ActorSystem("smsc-stub-server")
  implicit val timeout = Timeout(5.seconds)
  
  val smscPort = 10300
  val smppService = system.actorOf(Props[SmscStub], "smsc-stub")
  IO(Tcp) ! Bind(smppService, new InetSocketAddress("0.0.0.0", smscPort))

  val httpPort = 18888
  val httpService = system.actorOf(Props[SmscControl], "smsc-control")
  IO(Http) ? Http.Bind(httpService, interface = "0.0.0.0", port = httpPort)

}