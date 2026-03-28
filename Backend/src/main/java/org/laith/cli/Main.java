package org.laith.cli;

import org.laith.domain.enums.Role;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.Task;
import org.laith.domain.model.UserProfile;
import org.laith.exception.UserNotFoundException;
import org.laith.persistence.DbWriter;
import org.laith.service.TaskManager;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        boolean useDb = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--db")) useDb = true;
        }

        manager.setUseDb(useDb);
        manager.loadData(useDb);


        String requestedUserName = null;
        for (String arg : args) {
            if (arg.startsWith("--user=")) {
                requestedUserName = arg.substring("--user=".length()).trim();
            }
        }

        if (requestedUserName != null && !requestedUserName.isEmpty()) {
            Optional<UserProfile> maybeUser = manager.findUserByName(requestedUserName);
            if (maybeUser.isPresent()) {
                manager.setCurrentUser(maybeUser.get());
                System.out.println("Started with existing user from args: " + requestedUserName);
            } else {
                UserProfile newUser = manager.createUser(requestedUserName);
                manager.setCurrentUser(newUser);
                System.out.println("No existing user '" + requestedUserName +
                        "' found. Created new user and set as current.");
            }
        } else {
            List<UserProfile> users = manager.getUsers();
            if (!users.isEmpty()) {
                UserProfile first = users.get(0);
                manager.setCurrentUser(first);
                System.out.println("No user argument provided. Defaulting to first user: " + first.getUsername());
            } else {
                UserProfile defaultUser = manager.createUser("Player1");
                manager.setCurrentUser(defaultUser);
                System.out.println("No users found. Created default user: Player1");
            }
        }

        System.out.println();

        Scanner scanner = new Scanner(System.in);

        loginFlow(manager, scanner);

        boolean running = true;
        while (running) {
            printMainMenu(manager);
            String choice = scanner.nextLine().trim();

            UserProfile current = manager.getCurrentUser();
            boolean admin = current != null && current.getRole() == Role.ADMIN;

            if ("0".equals(choice)) {
                System.out.println("Saving data and exiting...");
                manager.saveData();
                break;
            }

            boolean handled = admin
                    ? handleAdminMenu(choice, manager, scanner)
                    : handlePlayerMenu(choice, manager, scanner);

            if (!handled) {
                System.out.println("Invalid option. Please try again.");
            }

            System.out.println();
        }

            scanner.close();
    }

    private static boolean handleAdminMenu(
            String choice,
            TaskManager manager,
            Scanner scanner
    ) {
        switch (choice) {
            case "1" -> createUserFlow(manager, scanner);
            case "2" -> switchUserFlow(manager, scanner);
            case "3" -> viewAllUsersFlow(manager);
            case "4" -> resetPlayerStatsFlow(manager, scanner);
            case "5" -> deleteUserFlow(manager, scanner);

            case "6" -> addTaskFlow(manager, scanner);
            case "7" -> manager.showTasksForCurrentUser();
            case "8" -> completeTaskFlow(manager, scanner);
            case "9" -> manager.showUserStats();
            case "10" -> manager.showLeaderboard();
            case "11" -> manager.showQuests();
            case "12" -> listWithFiltersFlow(manager, scanner);
            case "13" -> editTaskFlow(manager, scanner);
            case "14" -> deleteTaskFlow(manager, scanner);
            case "15" -> viewTaskDetailsFlow(manager, scanner);

            default -> { return false; }
        }
        return true;
    }

    private static boolean handlePlayerMenu(
            String choice,
            TaskManager manager,
            Scanner scanner
    ) {
        switch (choice) {
            case "1" -> addTaskFlow(manager, scanner);
            case "2" -> manager.showTasksForCurrentUser();
            case "3" -> completeTaskFlow(manager, scanner);
            case "4" -> manager.showUserStats();
            case "5" -> manager.showLeaderboard();
            case "6" -> manager.showQuests();
            case "7" -> listWithFiltersFlow(manager, scanner);
            case "8" -> editTaskFlow(manager, scanner);
            case "9" -> deleteTaskFlow(manager, scanner);
            case "10" -> viewTaskDetailsFlow(manager, scanner);

            default -> { return false; }
        }
        return true;
    }


    private static void printMainMenu(TaskManager manager) {
        System.out.println("=== Gamified Task Manager ===");
        UserProfile current = manager.getCurrentUser();

        if (current != null) {
            System.out.println("Current user: " + current.getUsername() +
                    " (role=" + current.getRole() +
                    ", id=" + current.getUserId() +
                    ", level=" + current.getLevel() +
                    ", points=" + current.getTotalPoints() +
                    ", streak=" + current.getCurrentStreakDays() + ")");
        }

        boolean admin = current != null && current.getRole() == Role.ADMIN;

        if (admin) {
            System.out.println("\n--- ADMIN SCENE ---");
            System.out.println("1. Create new player");
            System.out.println("2. Switch current player");
            System.out.println("3. View all users");
            System.out.println("4. Reset a player's stats");
            System.out.println("5. Delete a player");

            System.out.println("\n--- TASKS ---");
            System.out.println("6. Add task");
            System.out.println("7. List tasks");
            System.out.println("8. Complete task");
            System.out.println("9. Show stats");
            System.out.println("10. Leaderboard");
            System.out.println("11. Quests");
            System.out.println("12. List tasks with filters");
            System.out.println("13. Edit a task");
            System.out.println("14. Delete a task");
            System.out.println("15. View task details");
        } else {
            System.out.println("\n--- PLAYER SCENE ---");
            System.out.println("1. Add task");
            System.out.println("2. List tasks");
            System.out.println("3. Complete task");
            System.out.println("4. Show stats");
            System.out.println("5. Leaderboard");
            System.out.println("6. Quests");
            System.out.println("7. List tasks with filters");
            System.out.println("8. Edit a task");
            System.out.println("9. Delete a task");
            System.out.println("10. View task details");
        }

        System.out.println("0. Save & Exit");
        System.out.print("Choose an option: ");
    }


    private static void loginFlow(TaskManager manager, Scanner scanner) {
        while (true) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            try {
                if (!DbWriter.userHasPassword(username)) {
                    System.out.println("This user has no password yet. Please set one now.");
                    System.out.print("New password: ");
                    String p1 = scanner.nextLine();
                    System.out.print("Confirm password: ");
                    String p2 = scanner.nextLine();

                    if (!p1.equals(p2) || p1.isBlank()) {
                        System.out.println("Passwords don't match (or empty). Try again.\n");
                        continue;
                    }

                    DbWriter.setUserPasswordByUsername(username, p1);
                    System.out.println("✅ Password set. Now log in.\n");
                    // continue to login normally
                }

            } catch (Exception e) {
                System.out.println("Login setup error: " + e.getMessage());
                continue;
            }

            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (manager.loginDb(username, password)) {
                System.out.println("✅ Logged in.\n");
                return;
            }

            System.out.println("Invalid username/password. Try again.\n");
        }
    }



    // --- ADMIN HELPERS / FLOWS ---

    private static boolean requireAdmin(TaskManager manager) {
        UserProfile current = manager.getCurrentUser();
        if (current == null || current.getRole() != Role.ADMIN) {
            System.out.println("Access denied: ADMIN only.");
            return false;
        }
        return true;
    }

    private static void viewAllUsersFlow(TaskManager manager) {
        List<UserProfile> users = manager.getUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println("=== All Users ===");
        for (UserProfile u : users) {
            System.out.println("id=" + u.getUserId() +
                    " | username=" + u.getUsername() +
                    " | role=" + u.getRole() +
                    " | level=" + u.getLevel() +
                    " | points=" + u.getTotalPoints());
        }
    }

    private static void resetPlayerStatsFlow(TaskManager manager, Scanner scanner) {
        System.out.print("Enter user id to reset stats: ");
        int userId = readInt(scanner);
        manager.resetUserStats(userId);
    }

    private static void deleteUserFlow(TaskManager manager, Scanner scanner) {
        System.out.print("Enter user id to delete: ");
        int userId = readInt(scanner);
        manager.deleteUser(userId);
    }


    // MENU FLOWS

    private static void createUserFlow(TaskManager manager, Scanner scanner) {
        System.out.print("Enter new username: ");
        String username = readNonEmptyLine(scanner);
        manager.createUser(username);
    }

    private static void switchUserFlow(TaskManager manager, Scanner scanner) {
        List<UserProfile> users = manager.getUsers();
        if (users.isEmpty()) {
            System.out.println("No users available. Create a user first.");
            return;
        }

        System.out.println("Available users:");
        for (UserProfile user : users) {
            System.out.println("id=" + user.getUserId() + " | username=" + user.getUsername() + " | role=" + user.getRole());
        }

        System.out.print("Enter user id to switch to: ");
        int id = readInt(scanner);
        try {
            manager.switchCurrentUser(id);
        } catch (UserNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void addTaskFlow(TaskManager manager, Scanner scanner) {
        System.out.print("Task title: ");
        String title = readNonEmptyLine(scanner);

        System.out.print("Task description: ");
        String description = readNonEmptyLine(scanner);

        TaskType type = chooseTaskType(scanner);
        TaskDifficulty difficulty = chooseTaskDifficulty(scanner);
        TaskCategory category = chooseTaskCategory(scanner);

        LocalDate dueDate;
        if (type == TaskType.ONE_TIME) {
            dueDate = readOptionalDate(scanner);
        } else {
            LocalDate today = LocalDate.now();
            dueDate = switch (type) {
                case DAILY -> today;
                case WEEKLY -> today.plusWeeks(1);
                case MONTHLY -> today.plusMonths(1);
                default -> today;
            };
            System.out.println("Due date automatically set to: " + dueDate);
        }

        manager.addTaskForCurrentUser(title, description, type, difficulty, category, dueDate);
    }

    private static void completeTaskFlow(TaskManager manager, Scanner scanner) {
        System.out.print("Enter task id to complete: ");
        int taskId = readInt(scanner);
        manager.completeTaskForCurrentUser(taskId);
    }

    private static void deleteTaskFlow(TaskManager manager, Scanner scanner) {
        System.out.print("Enter task id to delete: ");
        int taskId = readInt(scanner);
        manager.deleteTaskForCurrentUser(taskId);
    }

    private static void editTaskFlow(TaskManager manager, Scanner scanner) {
        System.out.print("Enter task id to edit: ");
        int taskId = readInt(scanner);

        Optional<Task> maybeTask = manager.findTaskForCurrentUserById(taskId);
        if (maybeTask.isEmpty()) {
            System.out.println("Task with id " + taskId + " not found.");
            return;
        }

        Task task = maybeTask.get();
        System.out.println("Editing task: " + task);
        System.out.println("Leave input empty to keep current value.");

        System.out.print("New title: ");
        String newTitle = readOptionalLine(scanner).trim();
        if (newTitle.isEmpty()) newTitle = task.getTitle();

        System.out.print("New description: ");
        String newDesc = readOptionalLine(scanner).trim();
        if (newDesc.isEmpty()) newDesc = task.getDescription();

        TaskType newType = chooseOptionalTaskType(scanner);
        TaskDifficulty newDiff = chooseOptionalTaskDifficulty(scanner);
        TaskCategory newCat = chooseOptionalTaskCategory(scanner);


        TaskType effectiveType = (newType != null) ? newType : task.getType();

        LocalDate newDueDate;


        if (newType != null && newType != TaskType.ONE_TIME) {
            LocalDate today = LocalDate.now();
            newDueDate = switch (newType) {
                case DAILY -> today;
                case WEEKLY -> today.plusWeeks(1);
                case MONTHLY -> today.plusMonths(1);
                default -> today;
            };
            System.out.println("Due date automatically set to: " + newDueDate);
        } else {

            String currentShown = (task.getDueDate() == null) ? "none" : task.getDueDate().toString();
            System.out.print("Enter due date (YYYY-MM-DD) or press Enter to keep (" + currentShown + "): ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                newDueDate = task.getDueDate();
            } else {
                while (true) {
                    try {
                        newDueDate = LocalDate.parse(line);
                        break;
                    } catch (DateTimeParseException e) {
                        System.out.print("Invalid date. Use YYYY-MM-DD or press Enter to keep (" + currentShown + "): ");
                        line = scanner.nextLine().trim();
                        if (line.isEmpty()) {
                            newDueDate = task.getDueDate();
                            break;
                        }
                    }
                }
            }

            @SuppressWarnings("unused")
            TaskType _ignore = effectiveType;
        }

        manager.updateTaskForCurrentUser(
                taskId,
                newTitle,
                newDesc,
                newType,
                newDiff,
                newCat,
                newDueDate
        );
    }

    private static void listWithFiltersFlow(TaskManager manager, Scanner scanner) {
        List<Task> tasks = manager.getTasksForCurrentUser();
        if (tasks.isEmpty()) {
            System.out.println("No tasks to show.");
            return;
        }

        System.out.println("Choose filter:");
        System.out.println("1. All tasks");
        System.out.println("2. Pending only");
        System.out.println("3. Completed only");
        System.out.println("4. Overdue only");
        System.out.println("5. By Type");
        System.out.println("6. By Category");
        System.out.print("Your choice: ");

        String choice = scanner.nextLine().trim();

        List<Task> filtered = switch (choice) {
            case "1" -> tasks;
            case "2" -> tasks.stream().filter(t -> !t.isCompleted()).toList();
            case "3" -> tasks.stream().filter(Task::isCompleted).toList();
            case "4" -> tasks.stream().filter(Task::isOverdue).toList();
            case "5" -> {
                TaskType type = chooseTaskType(scanner);
                yield tasks.stream().filter(t -> t.getType() == type).toList();
            }
            case "6" -> {
                TaskCategory cat = chooseTaskCategory(scanner);
                yield tasks.stream().filter(t -> t.getCategory() == cat).toList();
            }
            default -> {
                System.out.println("Invalid choice. Showing all tasks.");
                yield tasks;
            }
        };

        if (filtered.isEmpty()) {
            System.out.println("No tasks match that filter.");
            return;
        }

        System.out.println("Filtered tasks:");
        for (Task t : filtered) {
            System.out.println(t);
        }
    }

    private static void viewTaskDetailsFlow(TaskManager manager, Scanner scanner) {
        System.out.print("Enter task id to view details: ");
        int taskId = readInt(scanner);
        manager.showTaskDetailsForCurrentUser(taskId);
    }

    // INPUT HELPERS

    private static String readNonEmptyLine(Scanner scanner) {
        while (true) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.print("Input cannot be empty. Try again: ");
        }
    }

    private static String readOptionalLine(Scanner scanner) {
        return scanner.nextLine().trim();
    }

    private static int readInt(Scanner scanner) {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid integer: ");
            }
        }
    }

    private static LocalDate readOptionalDate(Scanner scanner) {
        System.out.print("Enter due date (YYYY-MM-DD) or leave empty for no due date: ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return null;
        }
        while (true) {
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                System.out.print("Invalid date format. Please use YYYY-MM-DD or leave empty: ");
                line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    return null;
                }
            }
        }
    }

    private static TaskType chooseTaskType(Scanner scanner) {
        while (true) {
            System.out.println("Choose task type:");
            System.out.println("1. DAILY");
            System.out.println("2. WEEKLY");
            System.out.println("3. MONTHLY");
            System.out.println("4. ONE_TIME");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> { return TaskType.DAILY; }
                case "2" -> { return TaskType.WEEKLY; }
                case "3" -> { return TaskType.MONTHLY; }
                case "4" -> { return TaskType.ONE_TIME; }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static TaskType chooseOptionalTaskType(Scanner scanner) {
        while (true) {
            System.out.println("Change type? (enter to keep)");
            System.out.println("1. DAILY");
            System.out.println("2. WEEKLY");
            System.out.println("3. MONTHLY");
            System.out.println("4. ONE_TIME");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.isEmpty()) return null;

            switch (choice) {
                case "1" -> { return TaskType.DAILY; }
                case "2" -> { return TaskType.WEEKLY; }
                case "3" -> { return TaskType.MONTHLY; }
                case "4" -> { return TaskType.ONE_TIME; }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static TaskDifficulty chooseTaskDifficulty(Scanner scanner) {
        while (true) {
            System.out.println("Choose difficulty:");
            System.out.println("1. EASY");
            System.out.println("2. MEDIUM");
            System.out.println("3. HARD");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> { return TaskDifficulty.EASY; }
                case "2" -> { return TaskDifficulty.MEDIUM; }
                case "3" -> { return TaskDifficulty.HARD; }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static TaskDifficulty chooseOptionalTaskDifficulty(Scanner scanner) {
        while (true) {
            System.out.println("Change difficulty? (enter to keep)");
            System.out.println("1. EASY");
            System.out.println("2. MEDIUM");
            System.out.println("3. HARD");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.isEmpty()) return null;

            switch (choice) {
                case "1" -> { return TaskDifficulty.EASY; }
                case "2" -> { return TaskDifficulty.MEDIUM; }
                case "3" -> { return TaskDifficulty.HARD; }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static TaskCategory chooseTaskCategory(Scanner scanner) {
        while (true) {
            System.out.println("Choose category:");
            System.out.println("1. WORK");
            System.out.println("2. STUDY");
            System.out.println("3. HEALTH");
            System.out.println("4. PERSONAL");
            System.out.println("5. OTHER");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> { return TaskCategory.WORK; }
                case "2" -> { return TaskCategory.STUDY; }
                case "3" -> { return TaskCategory.HEALTH; }
                case "4" -> { return TaskCategory.PERSONAL; }
                case "5" -> { return TaskCategory.OTHER; }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    private static TaskCategory chooseOptionalTaskCategory(Scanner scanner) {
        while (true) {
            System.out.println("Change category? (enter to keep)");
            System.out.println("1. WORK");
            System.out.println("2. STUDY");
            System.out.println("3. HEALTH");
            System.out.println("4. PERSONAL");
            System.out.println("5. OTHER");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            if (choice.isEmpty()) return null;

            switch (choice) {
                case "1" -> { return TaskCategory.WORK; }
                case "2" -> { return TaskCategory.STUDY; }
                case "3" -> { return TaskCategory.HEALTH; }
                case "4" -> { return TaskCategory.PERSONAL; }
                case "5" -> { return TaskCategory.OTHER; }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }
}
