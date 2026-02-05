package io.github.amsatrio;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;

/**
 * KafkaAppender sends log events to an Apache Kafka topic.
 * <p>
 * This appender uses the {@link KafkaProducer} to dispatch encoded log events.
 * It requires a valid {@link Encoder} and a target topic name to be
 * operational.
 * </p>
 * *
 */
public class KafkaAppender extends AppenderBase<ILoggingEvent> {

    /**
     * Standard constructor for creating a new instance.
     */
    public KafkaAppender() {
        super();
    }

    /**
     * The underlying Kafka producer instance used to send messages.
     */
    private KafkaProducer<byte[], byte[]> producer;

    /**
     * The Kafka topic name where logs will be published.
     */
    private String topic;

    /**
     * Configuration properties for the Kafka producer.
     */
    private final Properties producerConfigs = new Properties();

    /**
     * The encoder responsible for converting logging events into byte arrays.
     */
    protected Encoder<ILoggingEvent> encoder;

    /**
     * Initializes the appender. Validates that the encoder and topic are set.
     * If validation passes, the Kafka producer is initialized.
     */
    @Override
    public void start() {
        if (this.encoder == null) {
            addError("No encoder set for the appender named [" + name + "].");
            return;
        }
        if (this.topic == null) {
            addError("No topic set for the appender named [" + name + "].");
            return;
        }

        initKafka();
        super.start();
    }

    /**
     * Encodes the logging event and asynchronously sends it to the configured Kafka
     * topic.
     * <p>
     * If the producer is not initialized or the appender is stopped, messages
     * are printed to the console as a fallback/warning.
     * </p>
     * * @param event The log event to be dispatched.
     */
    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            System.out.println("appender() - not started yet");
            return;
        }
        if (producer == null) {
            System.out.println("appender() - producer is null, initializing kafka..");
            initKafka();
        }

        try {
            byte[] encodedBytes = encoder.encode(event);

            ProducerRecord<byte[], byte[]> record = new ProducerRecord<>(topic, null,
                    encodedBytes);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    exception.printStackTrace();
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
            addError("Error while dispatching log to Kafka", exception);
        }
    }

    /**
     * Configures and instantiates the {@link KafkaProducer}.
     * Sets default properties such as serializers and acknowledgment levels.
     */
    private void initKafka() {
        if (producer != null) {
            System.out.println("initKafka() - Producer is not null, try to close it");
            producer.close();
        }

        producerConfigs.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);
        producerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        producerConfigs.put(ProducerConfig.LINGER_MS_CONFIG, 50);
        producerConfigs.put(ProducerConfig.ACKS_CONFIG, "all");

        try {
            producer = new KafkaProducer<>(producerConfigs);
        } catch (Exception exception) {
            addError("Failed to create Kafka Producer for [" + name + "]", exception);
            exception.printStackTrace();
        }
    }

    /**
     * Sets the encoder used to format log events.
     * 
     * @param encoder The layout encoder.
     */
    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    /**
     * Sets the Kafka topic to which logs should be sent.
     * 
     * @param topic The destination topic.
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Adds a raw configuration string (key=value) to the Kafka producer settings.
     * This is useful for passing custom properties from configuration files.
     * 
     * @param config A string in the format "key=value".
     */
    public void addProducerConfig(String config) {
        String[] parts = config.split("=", 2);
        if (parts.length == 2) {
            producerConfigs.put(parts[0].trim(), parts[1].trim());
        }
    }

    /**
     * Flushes and closes the Kafka producer to ensure all pending messages are sent
     * before the application shuts down.
     */
    @Override
    public void stop() {
        if (producer != null) {
            producer.flush();
            producer.close();
        }
        super.stop();
    }
}