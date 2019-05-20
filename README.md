# WebSocketManage
## 概要
WebSocket接続をうまくやってくれます、一定期間データが来ないときに再接続してくれます。
質問があったらTwitterまでどうぞ@take_btc

## 依存
```sbt:build.sbt
resolvers += "Maven Repo on github" at "https://BambooTuna.github.io/WebSocketManage/",
libraryDependencies = "com.github.BambooTuna" %% "WebSocketManage" % "1.0.-SNAPSHOT"
```