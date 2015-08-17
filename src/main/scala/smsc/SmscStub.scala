package smsc

import akka.actor._
import akka.io.Tcp._

class SmscStub extends Actor with ActorLogging {

  def receive = {

    case b @ Bound(localAddress) =>
      log.info("Bind to port {}", localAddress.getPort)

    case CommandFailed(_: Bind) =>
      log.info("CommandFailed: stopping")
      context stop self

    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[SmscHandler])
      sender ! Register(handler)

  }

}
