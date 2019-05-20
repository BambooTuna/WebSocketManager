package com.github.BambooTuna.WebSocketManage

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.Done
import com.github.BambooTuna.WebSocketManage.WebSocketProtocol._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class WebSocketActor(val webSocketOptions: WebSocketOptions)(implicit materializer: ActorMaterializer) extends Actor {
  implicit val system: ActorSystem = context.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  
  var wsInstance: Option[ActorRef] = None
  var timeoutCount = 0

  override def receive = {
    case ConnectStart => connect()
    case SendMessage(m) => send(m)
    case TimeoutCount =>
      timeoutCount += 1
      if (timeoutCount > webSocketOptions.pingTimeout) {
        timeoutCount = 0
        throw new WebSocketRuntimeException(s"Cannot get message timeout.")
      }
    case OnError(exception) => throw exception
    case other => webSocketOptions.logger.info(s"Get not defined method: $other")
  }

  override def preStart() = {
    super.preStart()
    if (webSocketOptions.reConnect) self ! ConnectStart
    webSocketOptions.logger.info("START")
  }

  override def postStop() = {
    super.postStop()
    wsInstance.foreach(w => context.stop(w))
    webSocketOptions.logger.info("STOP")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, None)
    webSocketOptions.logger.info("RESTART")
  }

  private def connect(): Unit = {
    webSocketOptions.logger.info("==========Connection Start==========")
    system.scheduler.schedule(1 seconds,  1 seconds, self, TimeoutCount)
    val ((ws, upgradeResponse), closed) = wsRunner().run()
    wsInstance = Some(ws)
    val connected = upgradeResponse.flatMap { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Future.successful(Done)
      } else {
        Future.failed(new WebSocketRuntimeException(s"Connection failed: ${upgrade.response.status}"))
      }
    }
    connected.onComplete {
      case Success(_) => context.parent ! ConnectedSucceeded(self)
      case Failure(exception) => throw exception
    }

    closed.map { _ =>
      self ! Closed
    }
  }

  private def send(data: String): Either[WebSocketError, Unit] = {
    wsInstance.toRight(SendMessageError).map(_ ! TextMessage.Strict(data))
  }

  private def wsRunner() = {
    val req = WebSocketRequest(webSocketOptions.host)
    val webSocketFlow = Http().webSocketClientFlow(req)

    val messageSource: Source[Message, ActorRef] = Source
      .actorRef[TextMessage.Strict](bufferSize = 1000, OverflowStrategy.fail)
      .keepAlive(webSocketOptions.pingInterval seconds, () => TextMessage.Strict("ping"))

    val messageSink = Flow[Message]
      .map{
        case TextMessage.Strict(m) => context.parent ! OnMessage(m)
        case BinaryMessage.Strict(m) => context.parent ! OnMessage(m.utf8String)
        case TextMessage.Streamed(stream) =>
          stream
            .limit(100)                   // Max frames we are willing to wait for
            .completionTimeout(5 seconds) // Max time until last frame
            .runFold("")(_ + _)
            .map(context.parent ! OnMessage(_))
        case other => self ! OnError(new WebSocketRuntimeException(s"Receive Strange Data: $other"))
      }.map(_ => timeoutCount = 0)

    messageSource
      .viaMat(webSocketFlow)(Keep.both)
      .via(messageSink)
      .toMat(Sink.ignore)(Keep.both)
  }

}

object WebSocketActor {
  val ActorName = "WebSocketActor"


}