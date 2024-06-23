import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

object WebSocketClientKafkaProducer {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val kafkaBrokers = "localhost:9092"
  val kafkaTopic = "websocket-topics"

  def main(args: Array[String]): Unit = {
    val kafkaProducer = createKafkaProducer()
    val webSocketFlow = createWebSocketFlow(kafkaProducer)
    
    Http().singleWebSocketRequest(
      request = akka.http.scaladsl.model.ws.WebSocketRequest(uri = "ws://localhost:8080/websocket"),
      clientFlow = webSocketFlow
    )
  }

  def createKafkaProducer(): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", kafkaBrokers)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    new KafkaProducer[String, String](props)
  }

  def createWebSocketFlow(producer: KafkaProducer[String, String]): Flow[Message, Message, Any] = {
    Flow[Message].collect {
      case TextMessage.Strict(text) =>
        producer.send(new ProducerRecord[String, String](kafkaTopic, text))
        TextMessage.Strict("Acknowledged")
    }
  }
}