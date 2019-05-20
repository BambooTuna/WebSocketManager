package com.github.BambooTuna.WebSocketManage

import com.github.BambooTuna.WebSocketManage.WebSocketProtocol._

import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props}
import akka.stream.ActorMaterializer
import akka.actor.SupervisorStrategy.{Restart, Stop}

import scala.concurrent.ExecutionContextExecutor

class WebSocketManager(val webSocketOptions: WebSocketOptions)(implicit materializer: ActorMaterializer) extends Actor {
  implicit val system: ActorSystem = context.system
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val webSocketActor = context.actorOf(Props(classOf[WebSocketActor], webSocketOptions, materializer), WebSocketActor.ActorName)

  override def receive = {
    case ConnectStart => webSocketActor ! ConnectStart
    case m: SendMessage => webSocketActor ! m
    case m: OnMessage => context.parent ! m
    case m: ConnectedSucceeded => context.parent ! m
    case other => webSocketOptions.logger.info(s"Get not defined method: $other")
  }

  override def supervisorStrategy = OneForOneStrategy() {
    case _ => if (webSocketOptions.reConnect) Restart else Stop
  }

  override def preStart() = {
    super.preStart()
  }

  override def postStop() = {
    super.postStop()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, None)
  }

}

object WebSocketManager {

  val ActorName = "WebSocketManager"

}
