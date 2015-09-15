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

  def shutdownStub: String = {
    log.info("Shutting down")
    val system = context.system
    import system.dispatcher

    import scala.concurrent.duration._
    import scala.language.postfixOps
    // TODO: Send unbind PDUs to bound ESMEs.
    system.scheduler.scheduleOnce(500 milliseconds) {
      system.shutdown()
    }
    "Shutting down\n"
  }
}

/**
 * Specification of the HTTP service.
 */
trait SmscControlService extends HttpService {

  def sendMoMessage(moMessage: MoMessage): String
  def shutdownStub: String

  /**
   * Specification of the `/send` URI as a Spray route.
   *
   * Query string parameters:
   *
   *  - `from`: the sending service number (e.g. a MSISDN like 61401001001)
   *  - `to`: the receiving service number (e.g. a PSMS service number like 1776)
   *  - `msg`: the message to send
   */
  val sendRoute = path("send") {
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
  /**
   * Specification of the `/shutdown` URI as a Spray route.
   */
  val shutdownRoute = path("shutdown") {
    get {
      respondWithMediaType(`text/plain`) {
        complete {
          shutdownStub
        }
      }
    }
  }
  /** Control is the combination of send and shutdown. */
  val controlRoute = sendRoute ~ shutdownRoute

}
