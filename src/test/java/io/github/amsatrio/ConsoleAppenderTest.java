package io.github.amsatrio;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsoleAppenderTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private ConsoleAppender appender;

    @BeforeEach
    public void setUp() {
        appender = new ConsoleAppender();
        appender.setName("TestAppender");
        // Redirect System.out to our captor
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        // Restore System.out after each test
        System.setOut(standardOut);
    }

    @Test
    void testAppend_ShouldPrintEncodedMessage() {
        // Arrange
        Encoder<ILoggingEvent> mockEncoder = mock(Encoder.class);
        ILoggingEvent mockEvent = mock(ILoggingEvent.class);
        String expectedMessage = "Log Message";
        
        when(mockEncoder.encode(mockEvent)).thenReturn(expectedMessage.getBytes());
        
        appender.setEncoder(mockEncoder);
        appender.start();

        // Act
        appender.append(mockEvent);

        // Assert
        assertEquals(expectedMessage, outputStreamCaptor.toString());
        verify(mockEncoder, times(1)).encode(mockEvent);
    }

    @Test
    void testStart_WithoutEncoder_ShouldNotStart() {
        // Act
        appender.start();

        // Assert
        assertFalse(appender.isStarted(), "Appender should not start without an encoder");
    }

    @Test
    void testStart_WithEncoder_ShouldStart() {
        // Arrange
        appender.setEncoder(mock(Encoder.class));

        // Act
        appender.start();

        // Assert
        assertTrue(appender.isStarted(), "Appender should start when encoder is present");
    }
}
