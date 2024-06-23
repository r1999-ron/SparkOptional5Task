name := "LogAnalysis"
version := "0.1"
scalaVersion := "2.12.18"

// Define Jackson version
val jacksonVersion = "2.12.5"
val sparkVersion = "3.2.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion, // For Kafka 0.10 integration
  "org.apache.spark" %% "spark-avro" % sparkVersion, // Added Spark Avro dependency
  "org.apache.hadoop" % "hadoop-common" % "3.2.0",
  "com.typesafe.akka" %% "akka-http" % "10.2.6",
  "com.typesafe.akka" %% "akka-stream" % "2.6.16",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.6",
  "org.apache.kafka" % "kafka-clients" % "2.7.0", // Kafka clients for producer/consumer
  "org.apache.avro" % "avro" % "1.10.2",
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "org.apache.spark" %% "spark-avro" % sparkVersion
)

// Exclude any transitive dependencies of Jackson Databind that might conflict
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion

// Fork in run and set java options
fork in run := true

javaOptions ++= Seq(
  "--add-opens=java.base/java.nio=ALL-UNNAMED",
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/java.lang=ALL-UNNAMED"
)