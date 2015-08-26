package smsc

import akka.actor._
import smpp.Deliver
import spray.http.MediaTypes._
import spray.routing._

class SmscControl extends Actor with ActorLogging with SmscControlService {

  override implicit def actorRefFactory = context
  override def receive = runRoute(controlRoute)

  def sendMoMessage(moMessage: MoMessage): String = {
    val deliverSm = Deliver.sm(moMessage)
    val smppHandler = context.actorOf(Props[SmscHandler])
    smppHandler ! deliverSm
    s"Seq: ${deliverSm.header.seqNumber}\n"
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
