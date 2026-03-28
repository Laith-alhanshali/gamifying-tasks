package org.laith.net;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TaskClient {


    public static void main(String[] args) {
        String host = "localhost";
        int port = 5555;

        try (
                Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                Scanner scanner = new Scanner(System.in)
        ) {
            readResponse(in);

            while (true) {
                System.out.print("> ");
                String cmd = scanner.nextLine();
                out.println(cmd);

                String full = readResponse(in);
                if (full == null) break;

                if (cmd.equalsIgnoreCase("EXIT")) break;
            }

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    private static String readResponse(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.equals("END")) {
                break;
            }
            sb.append(line).append(System.lineSeparator());
        }
        if (line == null) return null; // socket closed
        String text = sb.toString();
        System.out.print(text); // print as-is, already has newlines
        return text;
    }

}
