package smsc

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Tcp}

class SmscStub extends Actor with ActorLogging {

  import Tcp._

  val smscPort = 10300

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", smscPort))

  def receive = {

    case b @ Bound(localAddress) =>
      log.info("Bind to port {}", localAddress.getPort)

    case CommandFailed(_: smpp.Bind) => context stop self

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[SmscHandler])
      val connection = sender()
      connection ! Register(handler)

  }

}

object Main {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("smsc-stub-server")
    system.actorOf(Props[SmscStub])
  }

}