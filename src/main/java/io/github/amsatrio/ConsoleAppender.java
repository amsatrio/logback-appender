package io.github.amsatrio;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;

/**
 * ConsoleAppender appends log events to the standard output stream
 * (System.out).
 * <p>
 * This appender requires an {@link Encoder} to transform {@link ILoggingEvent}
 * objects into a string format suitable for the console.
 * </p>
 */
public class ConsoleAppender extends AppenderBase<ILoggingEvent> {

    /**
     * Standard constructor for creating a new instance.
     */
    public ConsoleAppender() {
        super();
    }

    /**
     * Checks for the presence of an encoder before starting the appender.
     * If no encoder is set, an error message is logged and the appender
     * will not start.
     */
    @Override
    public void start() {
        if (this.encoder == null) {
            addError("No encoder set for the appender named [" + name + "].");
            return;
        }
        super.start();
    }

    /**
     * Processes the logging event by encoding it and printing it to System.out.
     * 
     * @param event The logging event to be printed.
     */
    @Override
    protected void append(ILoggingEvent event) {
        byte[] encodedBytes = encoder.encode(event);
        String formattedMessage = new String(encodedBytes);
        System.out.print(formattedMessage);
    }

    /**
     * The encoder used to format the logging events.
     */
    protected Encoder<ILoggingEvent> encoder;

    /**
     * Sets the encoder to be used by this appender.
     * 
     * @param encoder The encoder to transform events into bytes.
     */
    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}