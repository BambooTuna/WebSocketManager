package com.github.BambooTuna.WebSocketManage

import akka.actor.ActorRef

object WebSocketProtocol {
  sealed trait WebSocketError
  case object SendMessageError extends WebSocketError
  class WebSocketRuntimeException(e: String) extends RuntimeException(e)

  sealed trait Receive
  case object ConnectStart extends Receive
  case class SendMessage(mes: String) extends Receive
  case object TimeoutCount extends Receive
  case class OnError(error: Exception) extends Receive

  sealed trait OutPut
  case class ConnectedSucceeded(actorRef: ActorRef) extends OutPut
  case class OnMessage(mes: String) extends OutPut
  case object Closed extends OutPut

}
