package com.github.amsatrio;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        // Redirect System.out to capture the "Hello World!" message
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        // Restore the original System.out
        System.setOut(standardOut);
    }

    @Test
    void testMainOutput() {
        // Act: Call the static main method
        App.main(new String[]{});

        // Assert: Check if the captured string matches (trimmed to ignore newlines)
        assertEquals("Hello World!", outputStreamCaptor.toString().trim());
    }
}
