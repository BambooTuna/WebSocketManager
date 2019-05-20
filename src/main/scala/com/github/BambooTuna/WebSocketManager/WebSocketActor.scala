package com.github.BambooTuna.WebSocketManager

import com.github.BambooTuna.WebSocketManager.WebSocketProtocol._

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.Done

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class WebSocketActor(val webSocketOptions: WebSocketOptions)(implicit materializer: ActorMaterializer) extends Actor {
  implicit val system: ActorSystem = context.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  
  var wsInstance: Option[ActorRef] = None
  var timeoutCount: FiniteDuration = (0 seconds)

  var timer: Option[Cancellable] = None

  override def receive = {
    case ConnectStart => connect()
    case SendMessage(m) => send(m)
    case TimeoutCount =>
      timeoutCount = timeoutCount.plus(1 seconds)
      if (timeoutCount > webSocketOptions.pingTimeout) {
        timer.foreach(stopTimer)
        timeoutCount = (0 seconds)
        self ! OnError(new WebSocketException(s"Cannot get message timeout."))
      }
    case OnError(exception) => throw exception
    case Closed => //self ! OnError(new WebSocketException(s"Connection closed."))
    case other => webSocketOptions.logger.info(s"Get not defined method: $other")
  }

  private def connect(): Unit = {
    val ((ws, upgradeResponse), closed) = wsRunner().run()
    wsInstance = Some(ws)
    val connected = upgradeResponse.flatMap { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        Future.successful(Done)
      } else {
        Future.failed(new WebSocketException(s"Connection failed: ${upgrade.response.status}"))
      }
    }

    connected.onComplete {
      case Success(_) =>
        webSocketOptions.logger.info("==========Connection Succeeded==========")
        setTimer()
        context.parent ! ConnectedSucceeded(self)
      case Failure(exception) => throw exception
    }

    closed.map { _ =>
      self ! Closed
    }
  }

  private def send(data: String): Unit = {
    wsInstance.fold(self ! OnError(new WebSocketException("Cannot get webSocket Instance.")))(_ ! TextMessage.Strict(data))
  }

  private def wsRunner() = {
    val req = WebSocketRequest(webSocketOptions.host)
    val webSocketFlow = Http().webSocketClientFlow(req)

    val messageSource: Source[Message, ActorRef] = Source
      .actorRef[TextMessage.Strict](bufferSize = 1000, OverflowStrategy.fail)
      .keepAlive(webSocketOptions.pingInterval, () => TextMessage.Strict(webSocketOptions.pingData))

    val messageSink = Flow[Message]
      .map{
        case TextMessage.Strict(m) => returnMessage(m)
        case BinaryMessage.Strict(m) => returnMessage(m.utf8String)
        case TextMessage.Streamed(stream) =>
          stream
            .limit(100)                   // Max frames we are willing to wait for
            .completionTimeout(5 seconds) // Max time until last frame
            .runFold("")(_ + _)
            .map(returnMessage)
        case other => self ! OnError(new WebSocketException(s"Receive Strange Data: $other"))
      }.map(_ => timeoutCount = 0 seconds)

    messageSource
      .viaMat(webSocketFlow)(Keep.both)
      .via(messageSink)
      .toMat(Sink.ignore)(Keep.both)
  }

  def returnMessage(message: String): Unit = {
    context.parent ! OnMessage(message)
  }

  override def preStart() = {
    super.preStart()
    timer.foreach(stopTimer)
    webSocketOptions.logger.info("START")
  }

  override def postStop() = {
    super.postStop()
    wsInstance.foreach(w => context.stop(w))
    timer.foreach(stopTimer)
    webSocketOptions.logger.info("STOP")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, None)
    webSocketOptions.logger.info("RESTARTING Wait...")
    Thread.sleep(webSocketOptions.reConnectInterval.toMillis)
    webSocketOptions.logger.info("RESTART")
    self ! ConnectStart
  }

  private def setTimer(): Unit = {
    timer.foreach(stopTimer)
    timer = Some(system.scheduler.schedule(1 seconds,  1 seconds, self, TimeoutCount))
  }

  private def stopTimer(t: Cancellable): Unit = {
    t.cancel()
  }

}

object WebSocketActor {

  val ActorName = "WebSocketActor"

}