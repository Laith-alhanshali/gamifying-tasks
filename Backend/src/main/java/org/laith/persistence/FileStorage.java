package org.laith.persistence;

import org.laith.domain.enums.Role;
import org.laith.service.Leaderboard;
import org.laith.domain.model.Rank;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.Task;
import org.laith.domain.model.UserProfile;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class FileStorage {

    private static final String USERS_FILE = "users.txt";
    private static final String TASKS_FILE = "tasks.txt";

    public static String getUsersFile() {
        return USERS_FILE;
    }

    public static String getTasksFile() {
        return TASKS_FILE;
    }


    public static void saveData(List<UserProfile> users) throws IOException {
        saveUsers(users);
        saveTasks(users);
    }

    private static void saveUsers(List<UserProfile> users) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (UserProfile user : users) {
                String achievementsCsv = String.join(",", user.getAchievements());

                String lastCompletionDateStr =
                        user.getLastCompletionDate() != null ? user.getLastCompletionDate().toString() : "null";

                String rankName = user.getCurrentRank() != null ? user.getCurrentRank().getName() : "null";

                String line = user.getUserId() + ";" +
                        sanitize(user.getUsername()) + ";" +
                        user.getRole().name() + ";" +
                        user.getTotalPoints() + ";" +
                        rankName + ";" +
                        user.getCurrentStreakDays() + ";" +
                        user.getLongestStreakDays() + ";" +
                        lastCompletionDateStr + ";" +
                        achievementsCsv;

                writer.write(line);
                writer.newLine();
            }
        }
    }


    private static void saveTasks(List<UserProfile> users) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TASKS_FILE))) {
            for (UserProfile user : users) {
                for (Task task : user.getTasks()) {
                    String dueDateStr = task.getDueDate() != null ? task.getDueDate().toString() : "null";
                    String createdAtStr = task.getCreatedAt() != null ? task.getCreatedAt().toString() : "null";
                    String completedAtStr = task.getCompletedAt() != null ? task.getCompletedAt().toString() : "null";

                    String line = task.getId() + ";" +
                            user.getUserId() + ";" +
                            sanitize(task.getTitle()) + ";" +
                            sanitize(task.getDescription()) + ";" +
                            task.getType().name() + ";" +
                            task.getDifficulty().name() + ";" +
                            task.getCategory().name() + ";" +
                            task.isCompleted() + ";" +
                            dueDateStr + ";" +
                            createdAtStr + ";" +
                            completedAtStr;

                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    // Simple sanitizing: replace ';' with ',' so we don't break our format
    private static String sanitize(String text) {
        if (text == null) return "";
        return text.replace(";", ",");
    }


    public static void loadData(List<UserProfile> users,
                                Leaderboard leaderboard,
                                List<Rank> rankDefinitions)
            throws IOException {

        users.clear();
        leaderboard.clear();

        Map<Integer, UserProfile> usersById = loadUsersInternal(users, leaderboard, rankDefinitions);
        loadTasksInternal(usersById);

    }

    private static Map<Integer, UserProfile> loadUsersInternal(List<UserProfile> users,
                                                               Leaderboard leaderboard,
                                                               List<Rank> rankDefinitions)
            throws IOException {

        Map<Integer, UserProfile> usersById = new HashMap<>();
        int maxUserId = 0;

        Map<String, Rank> rankByName = new HashMap<>();
        for (Rank rank : rankDefinitions) {
            rankByName.put(rank.getName(), rank);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split(";", -1);

                // Backward compatible:
                // Old format: 8 parts (no role)
                // New format: 9 parts (includes role)
                if (parts.length < 8) continue;

                int userId = Integer.parseInt(parts[0]);
                String username = parts[1];

                Role role;
                int totalPoints;
                String rankName;
                int currentStreak;
                int longestStreak;
                String lastCompletionDateStr;
                String achievementsCsv;

                if (parts.length >= 9) {
                    // NEW format
                    role = Role.valueOf(parts[2]);
                    totalPoints = Integer.parseInt(parts[3]);
                    rankName = parts[4];
                    currentStreak = Integer.parseInt(parts[5]);
                    longestStreak = Integer.parseInt(parts[6]);
                    lastCompletionDateStr = parts[7];
                    achievementsCsv = parts[8];
                } else {
                    // OLD format -> default role
                    role = Role.PLAYER;
                    totalPoints = Integer.parseInt(parts[2]);
                    rankName = parts[3];
                    currentStreak = Integer.parseInt(parts[4]);
                    longestStreak = Integer.parseInt(parts[5]);
                    lastCompletionDateStr = parts[6];
                    achievementsCsv = parts[7];
                }

                Rank rank = rankByName.get(rankName);
                LocalDate lastCompletionDate = !"null".equals(lastCompletionDateStr)
                        ? LocalDate.parse(lastCompletionDateStr)
                        : null;

                Set<String> achievements = new HashSet<>();
                if (achievementsCsv != null && !achievementsCsv.isBlank()) {
                    String[] achParts = achievementsCsv.split(",");
                    for (String a : achParts) {
                        if (!a.isBlank()) {
                            achievements.add(a.trim());
                        }
                    }
                }

                UserProfile user = new UserProfile(
                        userId,
                        username,
                        role,                // ✅ NEW
                        rank,
                        totalPoints,
                        currentStreak,
                        longestStreak,
                        lastCompletionDate,
                        achievements
                );

                users.add(user);
                leaderboard.addUser(user);
                usersById.put(userId, user);

                if (userId > maxUserId) {
                    maxUserId = userId;
                }
            }
        } catch (FileNotFoundException e) {
            throw e;
        }

        UserProfile.setNextId(maxUserId + 1);
        return usersById;
    }


    private static void loadTasksInternal(Map<Integer, UserProfile> usersById) throws IOException {
        int maxTaskId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(TASKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] parts = line.split(";", -1);
                if (parts.length < 11) {
                    continue;
                }

                int taskId = Integer.parseInt(parts[0]);
                int userId = Integer.parseInt(parts[1]);
                String title = parts[2];
                String description = parts[3];
                TaskType type = TaskType.valueOf(parts[4]);
                TaskDifficulty difficulty = TaskDifficulty.valueOf(parts[5]);
                TaskCategory category = TaskCategory.valueOf(parts[6]);
                boolean completed = Boolean.parseBoolean(parts[7]);

                String dueDateStr = parts[8];
                String createdAtStr = parts[9];
                String completedAtStr = parts[10];

                LocalDate dueDate = !"null".equals(dueDateStr) ? LocalDate.parse(dueDateStr) : null;
                LocalDateTime createdAt = !"null".equals(createdAtStr) ? LocalDateTime.parse(createdAtStr) : null;
                LocalDateTime completedAt = !"null".equals(completedAtStr) ? LocalDateTime.parse(completedAtStr) : null;

                UserProfile owner = usersById.get(userId);
                if (owner == null) {
                    continue;
                }

                Task task = new Task(
                        taskId,
                        title,
                        description,
                        type,
                        difficulty,
                        category,
                        dueDate,
                        completed,
                        createdAt,
                        completedAt
                );

                owner.addTask(task);

                if (taskId > maxTaskId) {
                    maxTaskId = taskId;
                }
            }
        } catch (FileNotFoundException e) {
            // No tasks file yet: fine on first run.
            throw e;
        }

        Task.setNextId(maxTaskId + 1);
    }
}
