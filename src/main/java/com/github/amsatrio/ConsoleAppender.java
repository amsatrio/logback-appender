package com.github.amsatrio;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;

public class ConsoleAppender extends AppenderBase<ILoggingEvent> {
    @Override
    public void start() {
        if (this.encoder == null) {
            addError("No encoder set for the appender named [" + name + "].");
            return;
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        byte[] encodedBytes = encoder.encode(event);
        String formattedMessage = new String(encodedBytes);
        System.out.print(formattedMessage);
    }

    protected Encoder<ILoggingEvent> encoder;
    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}

