package org.laith.net;

import org.laith.service.TaskManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TaskServer {

    public static void main(String[] args) {
        int port = 5555;
        TaskManager manager = new TaskManager();
        manager.loadData();

        System.out.println("TaskServer starting on port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket, manager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
