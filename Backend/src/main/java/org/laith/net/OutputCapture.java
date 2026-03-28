package org.laith.net;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class OutputCapture {

    public static String capture(Runnable action) {
        PrintStream originalOut = System.out;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream tempOut = new PrintStream(baos);

        try {
            System.setOut(tempOut);
            action.run();
        } finally {
            System.setOut(originalOut);
        }

        return baos.toString();
    }
}
