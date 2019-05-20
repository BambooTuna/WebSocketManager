package com.github.BambooTuna.WebSocketManage

import org.slf4j.{Logger, LoggerFactory}

case class WebSocketOptions(
                             host: String = "",
                             reConnect: Boolean = true,
                             pingInterval: Int = 10,
                             pingTimeout: Int = 20,
                             logger: Logger = LoggerFactory.getLogger(getClass)
                           ) {
  require(pingTimeout > pingInterval)
}
