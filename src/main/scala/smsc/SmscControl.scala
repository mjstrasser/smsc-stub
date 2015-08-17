package smsc

import akka.actor._
import spray.routing._
import spray.http._
import MediaTypes._
import smpp.{Deliver, DeliverSm}
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._

class SmscControl extends Actor with ActorLogging with SmscControlService {
  override implicit def actorRefFactory = context
  override def receive = runRoute(controlRoute)

  def sendMoMessage(moMessage: MoMessage): String = {
    val deliverSm = Deliver.sm(moMessage)
    val smppHandler = context.actorOf(Props[SmscHandler])
    smppHandler ! deliverSm
    "OK"
  }
}

trait SmscControlService extends HttpService {

  def sendMoMessage(moMessage: MoMessage): String

  val controlRoute = path("send") {
    get {
      parameters('sender, 'recipient, 'message).as(MoMessage) { moMessage =>
        respondWithMediaType(`text/plain`) {
          complete {
            sendMoMessage(moMessage)
          }
        }
      }
    }
  }

}
