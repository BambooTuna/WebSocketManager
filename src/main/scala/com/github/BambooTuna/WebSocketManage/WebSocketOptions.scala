package com.github.BambooTuna.WebSocketManage

import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.duration._
case class WebSocketOptions(
                             host: String = "",
                             reConnect: Boolean = true,
                             reConnectInterval: FiniteDuration = 5 seconds,
                             pingInterval: FiniteDuration = 5 seconds,
                             pingTimeout: FiniteDuration = 10 seconds,
                             pingData: String = "ping",
                             logger: Logger = LoggerFactory.getLogger("WebSocketManage")
                           ) {
  require(pingTimeout > pingInterval)
}

