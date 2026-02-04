package com.github.amsatrio;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;

public class KafkaAppender extends AppenderBase<ILoggingEvent> {
    private KafkaProducer<byte[], byte[]> producer;
    private String topic;
    private final Properties producerConfigs = new Properties();
    protected Encoder<ILoggingEvent> encoder;

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

    @Override
    protected void append(ILoggingEvent event) {

        if(!isStarted()){
            System.out.println("appender() - not started yet");
            return;
        }
        if(producer == null) {
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

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void addProducerConfig(String config) {
        String[] parts = config.split("=", 2);
        if (parts.length == 2) {
            producerConfigs.put(parts[0].trim(), parts[1].trim());
        }
    }

    @Override
    public void stop() {
        if (producer != null) {
            producer.flush();
            producer.close();
        }
        super.stop();
    }
}
