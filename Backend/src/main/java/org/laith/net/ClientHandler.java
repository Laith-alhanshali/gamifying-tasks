package org.laith.net;

import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.UserProfile;
import org.laith.service.TaskManager;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final TaskManager manager;

    // Each client has its own “current user”
    private UserProfile sessionUser;

    public ClientHandler(Socket socket, TaskManager manager) {
        this.socket = socket;
        this.manager = manager;
        this.sessionUser = null;
    }

    private void sendResponse(PrintWriter out, String response) {
        if (response == null) {
            out.println("OK");
            out.println("END");
            return;
        }

        // Send each line separately so the client can read them cleanly
        String[] lines = response.split("\\R", -1);
        for (String l : lines) {
            out.println(l);
        }
        out.println("END");
    }


    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            sendResponse(out, "OK Connected to Gamified Task Manager Server\n" +
                    "OK Commands: LOGIN <name> | LIST_TASKS | ADD_TASK|title|desc|type|diff|cat|dueDateOrNull | COMPLETE_TASK <id> | VIEW_TASK <id> | EXIT");

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.equalsIgnoreCase("EXIT")) {
                    sendResponse(out, "OK Bye");
                    break;
                }

                try {
                    String response = handleCommand(line);
                    sendResponse(out, response);

                } catch (Exception e) {
                    out.println("ERROR " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private String handleCommand(String line) {
        if (line.startsWith("LOGIN ")) {
            String name = line.substring("LOGIN ".length()).trim();
            if (name.isEmpty()) return "ERROR Username required";

            Optional<UserProfile> maybe = manager.findUserByName(name);

            // Keep it simple: create if missing
            UserProfile user = maybe.orElseGet(() -> manager.createUser(name));

            this.sessionUser = user;
            return "OK Logged in as " + user.getUsername() + " (role=" + user.getRole() + ", id=" + user.getUserId() + ")";
        }

        if (sessionUser == null) {
            return "ERROR Not logged in. Use: LOGIN <name>";
        }

        // IMPORTANT: TaskManager has a global currentUser in your design.
        // Synchronize + temporarily set current user for this request to avoid races.
        synchronized (manager) {
            manager.setCurrentUser(sessionUser);

            if (line.equalsIgnoreCase("LIST_TASKS")) {
                return OutputCapture.capture(() -> manager.showTasksForCurrentUser()).trim();
            }

            if (line.startsWith("COMPLETE_TASK ")) {
                int id = Integer.parseInt(line.substring("COMPLETE_TASK ".length()).trim());
                return OutputCapture.capture(() -> manager.completeTaskForCurrentUser(id)).trim();
            }

            if (line.startsWith("VIEW_TASK ")) {
                int id = Integer.parseInt(line.substring("VIEW_TASK ".length()).trim());
                return OutputCapture.capture(() -> manager.showTaskDetailsForCurrentUser(id)).trim();
            }

            if (line.startsWith("ADD_TASK|")) {
                // ADD_TASK|title|desc|type|diff|cat|dueDateOrNull
                String[] parts = line.split("\\|", -1);
                if (parts.length < 7) return "ERROR Invalid ADD_TASK format";

                String title = parts[1].trim();
                String desc = parts[2].trim();
                TaskType type = TaskType.valueOf(parts[3].trim());
                TaskDifficulty diff = TaskDifficulty.valueOf(parts[4].trim());
                TaskCategory cat = TaskCategory.valueOf(parts[5].trim());

                String dueStr = parts[6].trim();
                LocalDate due;
                if (!dueStr.equalsIgnoreCase("null") && !dueStr.isEmpty()) {
                    due = LocalDate.parse(dueStr);
                } else {
                    due = null;
                }

                return OutputCapture.capture(() -> manager.addTaskForCurrentUser(title, desc, type, diff, cat, due)).trim();
            }

            return "ERROR Unknown command";
        }
    }
}
