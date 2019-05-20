import Settings._

lazy val Sample =
  (project in file("sample"))
    .settings(commonSettings)
    .settings(
      name := "WebSocketManager-sample",
      libraryDependencies ++= Seq()
    )
    .dependsOn(root)

lazy val root =
  (project in file("."))
    .settings(commonSettings)
    .settings(
      name := "websocketmanager",
      version := "1.0.1-SNAPSHOT",
      crossScalaVersions := Seq("2.11.12", "2.12.4"),
      libraryDependencies ++= Seq(
        
      )
    )