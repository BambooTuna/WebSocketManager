# WebSocketManage
## 概要
WebSocket接続をうまくやってくれます、一定期間データが来ないときに再接続してくれます。  
質問があったらTwitterまでどうぞ@take_btc

## 依存
```sbt:build.sbt
resolvers += "Maven Repo on github" at "https://BambooTuna.github.io/WebSocketManage/",
libraryDependencies = "com.github.BambooTuna" %% "websocketmanager" % "1.0.0-SNAPSHOT"
```

## Options
```scala
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
```
- `reConnectInterval`
再接続時のインターバルです

- `pingInterval`
一定間隔でサーバーに`pingData`を送ります

- `pingTimeout`
この時間以上データーが来ないときに再接続します(`reConnect = true`のときのみ)  
`pingInterval`で送るpingに対するpongもデータ受信とみなします


## Sample
サンプルでは**BitFlyer**の`lightning_executions_FX_BTC_JPY`を購読する物を載せています  
[本家Doc](https://lightning.bitflyer.com/docs/playgroundrealtime)
