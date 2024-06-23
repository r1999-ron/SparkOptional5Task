import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import scala.concurrent.duration._
import java.util.Random

object WebSocketServer extends App {
  implicit val system: ActorSystem = ActorSystem("WebSocketServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val route =
    path("websocket") {
      get {
        handleWebSocketMessages(echoService)
      }
    }

  def echoService: Flow[Message, Message, Any] = {
    val random = new Random()
    val source = Source.tick(0.seconds, 30.seconds, akka.NotUsed)
        .map { _ =>
          val randomInt = random.nextInt(100)
          val jsonMessage = s"""{"process_time_ms": $randomInt, "threadname": "thread$randomInt", "memory_used_mb": $randomInt}"""
          TextMessage(jsonMessage)
        }
    
    Flow.fromSinkAndSourceCoupled(Sink.ignore, source)
  }

  Http().newServerAt("localhost", 8080).bind(route)
}