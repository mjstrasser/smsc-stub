package smsc

import akka.actor._
import smpp.Deliver
import spray.http.MediaTypes._
import spray.routing._

/**
 * HTTP-based control service implemented using Spray.
 *
 * It currently offers the URI `/send` that sends an MO message to [[SmscHandler]]
 * to deliver to a bound SMPP ESME.
 */
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

/**
 * Specification of the HTTP service.
 */
trait SmscControlService extends HttpService {

  def sendMoMessage(moMessage: MoMessage): String

  /**
   * Specification of the `/send` URI as a Spray route.
   *
   * Query string parameters:
   *
   *  - `from`: the sending service number (e.g. a MSISDN like 61401001001)
   *  - `to`: the receiving service number (e.g. a PSMS service number like 1776)
   *  - `msg`: the message to send
   */
  val controlRoute = path("send") {
    get {
      parameters('from, 'to, 'msg).as(MoMessage) { moMessage =>
        respondWithMediaType(`text/plain`) {
          complete {
            sendMoMessage(moMessage)
          }
        }
      }
    }
  }

}
