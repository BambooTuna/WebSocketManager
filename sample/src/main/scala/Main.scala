import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props}
import akka.stream.ActorMaterializer
import com.github.BambooTuna.WebSocketManage.WebSocketProtocol.{ConnectStart, ConnectedSucceeded, OnMessage, SendMessage}
import com.github.BambooTuna.WebSocketManage.{WebSocketActor, WebSocketManager, WebSocketOptions}
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._

object Main extends App {
  implicit val actorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  actorSystem.actorOf(Props(classOf[TestActor], materializer), "TestActor")
}


class TestActor(implicit materializer: ActorMaterializer) extends Actor {

  val webSocketManager = context.actorOf(Props(classOf[WebSocketManager], WebSocketOptions(
    host = "wss://ws.lightstream.bitflyer.com/json-rpc"
  ), materializer), WebSocketManager.ActorName)
  webSocketManager ! ConnectStart

  case class Channel(channel: String)
  case class Subscribe(method: String, params: Channel)
  val subscribeMessage = Subscribe("subscribe", Channel("lightning_executions_FX_BTC_JPY")).asJson.noSpaces

  override def receive = {
    case ConnectedSucceeded(ws) => ws ! SendMessage(subscribeMessage)
    case OnMessage(m) => println(m)
    case a => println(a)
  }

  override def supervisorStrategy = OneForOneStrategy() {
    case _ => Stop
  }

}