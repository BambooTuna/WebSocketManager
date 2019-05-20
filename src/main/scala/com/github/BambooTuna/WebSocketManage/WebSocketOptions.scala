package com.github.BambooTuna.WebSocketManage

import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.duration._
case class WebSocketOptions(
                             host: String = "",
                             reConnect: Boolean = true,
                             reConnectInterval: FiniteDuration = 5 seconds,
                             pingInterval: FiniteDuration = 1 seconds,
                             pingTimeout: FiniteDuration = 5 seconds,
                             logger: Logger = LoggerFactory.getLogger("WebSocketManage")
                           ) {
  require(pingTimeout > pingInterval)
}

