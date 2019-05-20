package com.github.BambooTuna.WebSocketManager

import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.duration._
case class WebSocketOptions(
                             host: String = "",
                             reConnect: Boolean = true,
                             reConnectInterval: FiniteDuration = 5 seconds,
                             pingInterval: FiniteDuration = 5 seconds,
                             pingTimeout: FiniteDuration = 10 seconds,
                             pingData: String = "ping",
                             logger: Logger = LoggerFactory.getLogger("WebSocketManager")
                           ) {
  require(pingTimeout > pingInterval)
}

