package org.laith.service;

import org.laith.domain.enums.Role;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.*;
import org.laith.exception.TaskNotFoundException;
import org.laith.exception.UserNotFoundException;
import org.laith.persistence.DbStorage;
import org.laith.persistence.DbWriter;
import org.laith.persistence.FileStorage;
import org.laith.service.quests.DailyQuest;
import org.laith.service.quests.WeeklyChallenge;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TaskManager {

    private final List<UserProfile> users;
    private UserProfile currentUser;
    private final Leaderboard leaderboard;
    private final List<Rank> rankDefinitions;
    private boolean useDb = false;

    private final DailyQuest dailyQuest;
    private final WeeklyChallenge weeklyChallenge;

    public TaskManager() {
        this.leaderboard = new Leaderboard();
        this.rankDefinitions = createDefaultRanks();
        this.users = new ArrayList<>();

        this.dailyQuest = new DailyQuest(TaskCategory.HEALTH, 3);
        this.weeklyChallenge = new WeeklyChallenge(15);

        this.currentUser = null;
    }

    private List<Rank> createDefaultRanks() {
        List<Rank> ranks = new ArrayList<>();
        ranks.add(new Rank("Bronze", 0));
        ranks.add(new Rank("Silver", 100));
        ranks.add(new Rank("Gold", 300));
        ranks.add(new Rank("Platinum", 700));
        ranks.add(new Rank("Diamond", 1500));
        return ranks;
    }

    private boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public void setUseDb(boolean useDb) {
        this.useDb = useDb;
    }

    public boolean loginDb(String username, String password) {
        if (!useDb) {
            System.out.println("Login only implemented for DB mode.");
            return false;
        }

        try {
            Integer userId = DbWriter.authenticate(username, password);
            if (userId == null) {
                System.out.println("Invalid username/password.");
                return false;
            }

            Optional<UserProfile> maybe = findUserById(userId);
            if (maybe.isEmpty()) {
                System.out.println("User authenticated but not loaded. Reloading...");
                loadData(true);
                maybe = findUserById(userId);
            }

            if (maybe.isEmpty()) {
                System.out.println("Login failed: user not found after load.");
                return false;
            }

            setCurrentUser(maybe.get());
            return true;

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            return false;
        }
    }


    // ---------- ADMIN OPS (optional) ----------

    public void resetUserStats(int userId) {
        if (!isAdmin()) {
            System.out.println("Access denied: ADMIN only.");
            return;
        }

        Optional<UserProfile> maybe = findUserById(userId);
        if (maybe.isEmpty()) {
            System.out.println("User with id " + userId + " not found.");
            return;
        }

        UserProfile u = maybe.get();

        // don't allow resetting yourself (optional safety)
        if (currentUser != null && u.getUserId() == currentUser.getUserId()) {
            System.out.println("You cannot reset the currently logged-in admin.");
            return;
        }

        // DB reset first
        if (useDb) {
            try {
                DbWriter.resetUserStats(userId);
            } catch (Exception e) {
                System.out.println("DB error resetting user: " + e.getMessage());
                return;
            }
        }

        // in-memory reset by rebuilding a fresh profile (role preserved)
        UserProfile fresh = new UserProfile(
                u.getUserId(),
                u.getUsername(),
                u.getRole(),
                rankDefinitions.get(0), // Bronze
                0, 0, 0,
                null,
                new HashSet<>()
        );

        // replace in list
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId() == userId) {
                users.set(i, fresh);
                break;
            }
        }

        // rebuild leaderboard (simple and safe)
        leaderboard.clear();
        for (UserProfile p : users) leaderboard.addUser(p);

        System.out.println("✅ Reset completed for user: " + u.getUsername());
    }

    public void deleteUser(int userId) {
        if (!isAdmin()) {
            System.out.println("Access denied: ADMIN only.");
            return;
        }

        // don't allow deleting yourself (strongly recommended)
        if (currentUser != null && userId == currentUser.getUserId()) {
            System.out.println("You cannot delete the currently logged-in admin.");
            return;
        }

        Optional<UserProfile> maybe = findUserById(userId);
        if (maybe.isEmpty()) {
            System.out.println("User with id " + userId + " not found.");
            return;
        }

        // DB delete first
        if (useDb) {
            try {
                DbWriter.deleteUser(userId);
            } catch (Exception e) {
                System.out.println("DB error deleting user: " + e.getMessage());
                return;
            }
        }

        // in-memory delete
        users.removeIf(u -> u.getUserId() == userId);

        // rebuild leaderboard
        leaderboard.clear();
        for (UserProfile u : users) leaderboard.addUser(u);

        System.out.println("✅ User deleted successfully (id=" + userId + ")");
    }



    // ---------- USER MANAGEMENT ----------

    public UserProfile createUser(String username, Role role) {
        // You had this restriction: if there are already users, only admin can create more
        if (!users.isEmpty() && !isAdmin()) {
            System.out.println("Access denied: only ADMIN can create users.");
            return null;
        }

        // DB mode: insert first, then build UserProfile with returned id
        if (useDb) {
            try {
                int newId = DbWriter.insertUser(username, role);

                // achievements empty at creation
                Set<String> achievements = new HashSet<>();

                UserProfile user = new UserProfile(
                        newId,
                        username,
                        role,
                        rankDefinitions.get(0), // Bronze
                        0,
                        0,
                        0,
                        null,
                        achievements
                );

                users.add(user);
                leaderboard.addUser(user);

                System.out.println("Created DB user " + username + " (" + role + ") with id " + user.getUserId());
                return user;

            } catch (Exception e) {
                System.out.println("DB error creating user: " + e.getMessage());
                return null;
            }
        }

        // File/in-memory mode
        UserProfile user = new UserProfile(username, rankDefinitions.get(0), role);
        users.add(user);
        leaderboard.addUser(user);
        System.out.println("Created user " + username + " (" + role + ") with id " + user.getUserId());
        return user;
    }

    public UserProfile createUser(String username) {
        return createUser(username, Role.PLAYER);
    }

    public List<UserProfile> getUsers() {
        return new ArrayList<>(users);
    }

    public Optional<UserProfile> findUserById(int userId) {
        return users.stream().filter(u -> u.getUserId() == userId).findFirst();
    }

    public Optional<UserProfile> findUserByName(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
        System.out.println("Current user switched to: " + user.getUsername() +
                " (id=" + user.getUserId() + ")");
    }

    public void switchCurrentUser(int userId) throws UserNotFoundException {
        if (!isAdmin()) {
            System.out.println("Access denied: only ADMIN can switch users.");
            return;
        }
        Optional<UserProfile> maybeUser = findUserById(userId);
        if (maybeUser.isEmpty()) throw new UserNotFoundException(userId);
        setCurrentUser(maybeUser.get());
    }

    public UserProfile getCurrentUser() {
        return currentUser;
    }

    public List<Rank> getRankDefinitions() {
        return new ArrayList<>(rankDefinitions);
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    // ---------- TASK MANAGEMENT ----------

    public void addTaskForCurrentUser(String title,
                                      String description,
                                      TaskType type,
                                      TaskDifficulty difficulty,
                                      TaskCategory category,
                                      LocalDate dueDate) {
        if (currentUser == null) {
            System.out.println("No current user selected. Create or switch to a user first.");
            return;
        }

        // DB mode: insert first (to get real DB id), then construct Task with that id
        if (useDb) {
            try {
                int taskId = DbWriter.insertTask(
                        currentUser.getUserId(),
                        title,
                        description,
                        type,
                        difficulty,
                        category,
                        dueDate
                );

                Task task = new Task(
                        taskId,
                        title,
                        description,
                        type,
                        difficulty,
                        category,
                        dueDate,
                        false,
                        LocalDateTime.now(),
                        null
                );

                currentUser.addTask(task);
                System.out.println("Task created with id " + task.getId());
                return;

            } catch (Exception e) {
                System.out.println("DB error adding task: " + e.getMessage());
                return;
            }
        }

        // File/in-memory mode
        Task task = new Task(title, description, type, difficulty, category, dueDate);
        currentUser.addTask(task);
        System.out.println("Task created with id " + task.getId());
    }

    public void completeTaskForCurrentUser(int taskId) {
        if (currentUser == null) {
            System.out.println("No current user selected.");
            return;
        }

        Optional<Task> maybeTask = currentUser.findTaskById(taskId);
        if (maybeTask.isEmpty()) {
            System.out.println("Task with id " + taskId + " not found.");
            return;
        }

        Task task = maybeTask.get();

        try {
            currentUser.completeTask(taskId, rankDefinitions);

            // quests/challenges
            dailyQuest.onTaskCompleted(task, currentUser);
            weeklyChallenge.onTaskCompleted(task, currentUser);

            // DB persistence for completion + user stats + achievements
            if (useDb) {
                if (task.getCompletedAt() != null) {
                    DbWriter.markTaskCompleted(task.getId(), task.getCompletedAt());
                }
                DbWriter.updateUserAfterProgress(currentUser);
                DbWriter.replaceAchievements(currentUser.getUserId(), currentUser.getAchievements());
            }

            // recurring next occurrence (also insert if DB mode)
            createNextRecurringOccurrenceIfNeeded(task);

        } catch (TaskNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("DB error after completing task: " + e.getMessage());
        }
    }

    public void showUserStats() {
        if (currentUser == null) {
            System.out.println("No current user selected.");
            return;
        }
        currentUser.printStats();
    }

    public void showTasksForCurrentUser() {
        if (currentUser == null) {
            System.out.println("No current user selected.");
            return;
        }
        System.out.println("Tasks for " + currentUser.getUsername() + " (id=" + currentUser.getUserId() + "):");
        for (Task task : currentUser.getTasks()) {
            System.out.println(task);
        }
    }

    public void showLeaderboard() {
        System.out.println("=== Leaderboard ===");
        int position = 1;
        for (UserProfile user : leaderboard.getAllUsersSorted()) {
            System.out.println(position + ". [id=" + user.getUserId() + "] " + user.getUsername() +
                    " - " + user.getTotalPoints() + " points (" +
                    (user.getCurrentRank() != null ? user.getCurrentRank().getName() : "No Rank") +
                    ")");
            position++;
        }
    }

    public void showQuests() {
        System.out.println("=== Quests & Challenges ===");
        System.out.println("Daily Quest: " + dailyQuest);
        System.out.println("Weekly Challenge: " + weeklyChallenge);
    }

    public void showTaskDetailsForCurrentUser(int taskId) {
        if (currentUser == null) {
            System.out.println("No current user selected.");
            return;
        }

        Optional<Task> maybeTask = currentUser.findTaskById(taskId);
        if (maybeTask.isEmpty()) {
            System.out.println("Task with id " + taskId + " not found.");
            return;
        }

        Task t = maybeTask.get();
        System.out.println("=== Task Details ===");
        System.out.println("ID: " + t.getId());
        System.out.println("Title: " + t.getTitle());
        System.out.println("Description: " + t.getDescription());
        System.out.println("Type: " + t.getType());
        System.out.println("Difficulty: " + t.getDifficulty());
        System.out.println("Category: " + t.getCategory());
        System.out.println("Status: " + (t.isCompleted() ? "COMPLETED" : (t.isOverdue() ? "OVERDUE" : "PENDING")));
        System.out.println("Due date: " + t.getDueDate());
        System.out.println("Created at: " + t.getCreatedAt());
        System.out.println("Completed at: " + t.getCompletedAt());
        System.out.println("Points value: " + t.calculatePoints());
    }

    private void createNextRecurringOccurrenceIfNeeded(Task completedTask) {
        if (completedTask == null) return;

        TaskType type = completedTask.getType();
        if (type == TaskType.ONE_TIME) return;

        LocalDate baseDue = completedTask.getDueDate();
        if (baseDue == null) baseDue = LocalDate.now();

        LocalDate nextDue = switch (type) {
            case DAILY -> baseDue.plusDays(1);
            case WEEKLY -> baseDue.plusWeeks(1);
            case MONTHLY -> baseDue.plusMonths(1);
            default -> baseDue;
        };

        // DB mode: insert next occurrence, then create Task with returned id
        if (useDb) {
            try {
                int newTaskId = DbWriter.insertTask(
                        currentUser.getUserId(),
                        completedTask.getTitle(),
                        completedTask.getDescription(),
                        completedTask.getType(),
                        completedTask.getDifficulty(),
                        completedTask.getCategory(),
                        nextDue
                );

                Task next = new Task(
                        newTaskId,
                        completedTask.getTitle(),
                        completedTask.getDescription(),
                        completedTask.getType(),
                        completedTask.getDifficulty(),
                        completedTask.getCategory(),
                        nextDue,
                        false,
                        LocalDateTime.now(),
                        null
                );

                currentUser.addTask(next);
                System.out.println("🔁 Recurring task created: new occurrence id=" + next.getId() + " due=" + nextDue);
                return;

            } catch (Exception e) {
                System.out.println("DB error creating recurring task: " + e.getMessage());
                // fall through to in-memory creation if you want; here we stop to avoid desync
                return;
            }
        }

        // File/in-memory mode
        Task next = new Task(
                completedTask.getTitle(),
                completedTask.getDescription(),
                completedTask.getType(),
                completedTask.getDifficulty(),
                completedTask.getCategory(),
                nextDue
        );

        currentUser.addTask(next);
        System.out.println("🔁 Recurring task created: new occurrence id=" + next.getId() + " due=" + nextDue);
    }

    // ---------- TASK HELPERS ----------

    public List<Task> getTasksForCurrentUser() {
        if (currentUser == null) return List.of();
        return currentUser.getTasks();
    }

    public Optional<Task> findTaskForCurrentUserById(int taskId) {
        if (currentUser == null) return Optional.empty();
        return currentUser.findTaskById(taskId);
    }

    public boolean deleteTaskForCurrentUser(int taskId) {
        if (currentUser == null) {
            System.out.println("No current user selected.");
            return false;
        }

        boolean removed = currentUser.removeTaskById(taskId);

        if (!removed) {
            System.out.println("Task with id " + taskId + " not found.");
            return false;
        }

        // DB delete
        if (useDb) {
            try {
                DbWriter.deleteTask(taskId);
            } catch (Exception e) {
                System.out.println("DB error deleting task: " + e.getMessage());
            }
        }

        System.out.println("Task deleted successfully.");
        return true;
    }

    public boolean updateTaskForCurrentUser(int taskId,
                                            String newTitle,
                                            String newDescription,
                                            TaskType newType,
                                            TaskDifficulty newDifficulty,
                                            TaskCategory newCategory,
                                            LocalDate newDueDate) {
        if (currentUser == null) {
            System.out.println("No current user selected.");
            return false;
        }

        Optional<Task> maybeTask = currentUser.findTaskById(taskId);
        if (maybeTask.isEmpty()) {
            System.out.println("Task with id " + taskId + " not found.");
            return false;
        }

        Task task = maybeTask.get();

        if (newTitle != null) task.setTitle(newTitle);
        if (newDescription != null) task.setDescription(newDescription);
        if (newType != null) task.setType(newType);
        if (newDifficulty != null) task.setDifficulty(newDifficulty);
        if (newCategory != null) task.setCategory(newCategory);
        task.setDueDate(newDueDate);

        // DB update (use the task's effective values)
        if (useDb) {
            try {
                DbWriter.updateTask(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getType(),
                        task.getDifficulty(),
                        task.getCategory(),
                        task.getDueDate()
                );
            } catch (Exception e) {
                System.out.println("DB error updating task: " + e.getMessage());
            }
        }

        System.out.println("Task updated successfully.");
        return true;
    }

    // ---------- LOAD/SAVE ----------

    public void loadData(boolean useDb) {
        try {
            this.useDb = useDb;

            if (useDb) {
                DbStorage.loadData(users, leaderboard, rankDefinitions);
                System.out.println("Loaded from DB: " + users.size() + " users.");
            } else {
                FileStorage.loadData(users, leaderboard, rankDefinitions);
                System.out.println("Loaded from files: " + users.size() + " users.");
            }
        } catch (Exception e) {
            System.out.println("Load error: " + e.getMessage());
        }
    }

    // keep old file-only methods if you still want them
    public void loadData() {
        try {
            FileStorage.loadData(users, leaderboard, rankDefinitions);
            if (!users.isEmpty()) {
                System.out.println("Data loaded successfully. Loaded " + users.size() + " users.");
            } else {
                System.out.println("No users found in file. You can create a new one from the menu.");
            }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("No saved data found (" + e.getMessage() + "). Starting fresh.");
        } catch (java.io.IOException e) {
            System.out.println("Error while loading data: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Data format error while loading: " + e.getMessage());
        }
    }

    public void saveData() {
        if (useDb) {
            // In DB mode you usually don't need a "save all" (each operation persists immediately).
            System.out.println("DB mode: changes are saved immediately.");
            return;
        }

        try {
            FileStorage.saveData(users);
            System.out.println("Data saved successfully.");
        } catch (java.io.IOException e) {
            System.out.println("Error while saving data: " + e.getMessage());
        }
    }
}
