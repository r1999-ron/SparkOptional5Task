import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.avro._
import org.apache.spark.sql.functions._

object KafkaSparkConsumer {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("KafkaSparkConsumer")
      .master("local[*]")
      .config("spark.driver.bindAddress", "127.0.0.1")
      .config("spark.driver.port", "7077")
      .config("spark.ui.port", "4040")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.sql.shuffle.partitions", "10")
      .getOrCreate()

    // Read from Kafka
    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "websocket-topics")
      .option("startingOffsets", "earliest")
      .option("kafka.max.partition.fetch.bytes", "10485760")
      .load()

    // Define the schema
    val avroSchema = """{
      "type": "record",
      "name": "WebSocketRecord",
      "fields": [
        {"name": "process_time_ms", "type": "int"},
        {"name": "threadname", "type": "string"},
        {"name": "memory_used_mb", "type": "int"}
      ]
    }"""

    // Deserialize the Avro data
    val valueDF = df.select(from_avro(col("value"), avroSchema).as("data")).select("data.*")

    // Add a column for event time
    val withEventTimeDF = valueDF.withColumn("event_time", current_timestamp())

    // Define watermark on event_time
    val withWatermarkDF = withEventTimeDF.withWatermark("event_time", "10 minutes")

    // Perform aggregation operations
    val aggDF = withWatermarkDF.groupBy(
      col("threadname"), 
      window(col("event_time"), "5 minutes")  // Windowing based on event time
    ).agg(
      avg("process_time_ms").alias("avg_process_time"),
      sum("memory_used_mb").alias("total_memory_used"),
      max("process_time_ms").alias("max_process_time")
    )

    val query = aggDF.writeStream
         .outputMode("append")
         .format("json")
         .option("path", "/Users/ronak/Downloads/kafka_aggregation_result_spark_folder/json")
         .option("checkpointLocation", "/Users/ronak/Downloads/checkpoint2187_json")
         .start()

    query.awaitTermination()
    
  }
}