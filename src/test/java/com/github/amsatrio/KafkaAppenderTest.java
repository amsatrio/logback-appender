package com.github.amsatrio;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaAppenderTest {

    private KafkaAppender appender;
    private Encoder<ILoggingEvent> mockEncoder;
    private ILoggingEvent mockEvent;
    private MockedConstruction<KafkaProducer> mockedProducerConstruction;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        appender = new KafkaAppender();
        mockEncoder = mock(Encoder.class);
        mockEvent = mock(ILoggingEvent.class);
        
        mockedProducerConstruction = mockConstruction(KafkaProducer.class);
        
        appender.setEncoder(mockEncoder);
        appender.setTopic("test-topic");
        appender.setName("KafkaAppenderTest");
    }

    @AfterEach
    void tearDown() {
        mockedProducerConstruction.close();
    }

    @Test
    void testStart_Success() {
        appender.start();
        
        assertTrue(appender.isStarted());
        assertEquals(1, mockedProducerConstruction.constructed().size());
    }

    @Test
    void testStart_MissingEncoder() {
        appender.setEncoder(null);
        appender.start();
        
        assertFalse(appender.isStarted());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAppend_SendsRecord() {
        appender.start();
        
        byte[] payload = "log message".getBytes();
        when(mockEncoder.encode(mockEvent)).thenReturn(payload);
        
        appender.append(mockEvent);

        KafkaProducer<byte[], byte[]> mockProducer = mockedProducerConstruction.constructed().get(0);
        ArgumentCaptor<ProducerRecord<byte[], byte[]>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        
        // Verify send was called
        verify(mockProducer).send(recordCaptor.capture(), any());
        
        // Verify record content
        ProducerRecord<byte[], byte[]> capturedRecord = recordCaptor.getValue();
        assertEquals("test-topic", capturedRecord.topic());
        assertArrayEquals(payload, capturedRecord.value());
    }

    @Test
    void testAddProducerConfig_ParsesCorrectly() {
        appender.addProducerConfig("bootstrap.servers=localhost:9092");
        appender.start();
        
        assertTrue(appender.isStarted());
    }

    @Test
    void testStop_ClosesProducer() {
        appender.start();
        appender.stop();

        KafkaProducer<byte[], byte[]> mockProducer = mockedProducerConstruction.constructed().get(0);
        verify(mockProducer).flush();
        verify(mockProducer).close();
        assertFalse(appender.isStarted());
    }
}