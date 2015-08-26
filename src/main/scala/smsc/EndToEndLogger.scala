package smsc

import akka.actor._
import smpp.{DeliverSm, SubmitSm}

class EndToEndLogger extends Actor with ActorLogging {

  def receive = {

    case SubmitSm(_, submitBody) =>
      log.info("{},{},{}", submitBody.sourceAddr, submitBody.destinationAddr, submitBody.shortMessage)

    case DeliverSm(_, deliverBody) =>
      log.info("{},{},{}", deliverBody.sourceAddr, deliverBody.destinationAddr, deliverBody.shortMessage)

    case _ =>

  }

}
