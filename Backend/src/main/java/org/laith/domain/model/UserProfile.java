package org.laith.domain.model;

import org.laith.service.MotivationalMessages;
import org.laith.exception.TaskNotFoundException;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskType;
import org.laith.domain.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class UserProfile {


    private static int NEXT_ID = 1;

    private final int userId;
    private final String username;
    private final Role role;
    private int totalPoints;
    private int level;
    private Rank currentRank;
    private int currentStreakDays;
    private int longestStreakDays;
    private LocalDate lastCompletionDate;

    private final List<Task> tasks;
    private final Set<String> achievements;

    // Constructor for NEW users created in the app
    public UserProfile(String username, Rank initialRank, Role role) {
        this.userId = NEXT_ID++;
        this.username = username;
        this.role = role;
        this.currentRank = initialRank;
        this.totalPoints = 0;
        this.currentStreakDays = 0;
        this.longestStreakDays = 0;
        this.lastCompletionDate = null;
        this.tasks = new ArrayList<>();
        this.achievements = new HashSet<>();
        updateLevel();
    }

    // Constructor for loading from file with full state
    public UserProfile(int userId,
                       String username,
                       Role role,
                       Rank currentRank,
                       int totalPoints,
                       int currentStreakDays,
                       int longestStreakDays,
                       LocalDate lastCompletionDate,
                       Set<String> achievements) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.currentRank = currentRank;
        this.totalPoints = totalPoints;
        this.currentStreakDays = currentStreakDays;
        this.longestStreakDays = longestStreakDays;
        this.lastCompletionDate = lastCompletionDate;
        this.tasks = new ArrayList<>();
        this.achievements = new HashSet<>(achievements);
        updateLevel();
    }

    public int getUserId() {
        return userId;
    }

    public static void setNextId(int nextId) {
        NEXT_ID = nextId;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public Rank getCurrentRank() {
        return currentRank;
    }

    public int getCurrentStreakDays() {
        return currentStreakDays;
    }

    public int getLongestStreakDays() {
        return longestStreakDays;
    }

    public LocalDate getLastCompletionDate() {
        return lastCompletionDate;
    }

    public int getLevel() {
        return level;
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public boolean removeTaskById(int taskId) {
        return tasks.removeIf(task -> task.getId() == taskId);
    }

    public Optional<Task> findTaskById(int taskId) {
        return tasks.stream()
                .filter(task -> task.getId() == taskId)
                .findFirst();
    }

    public List<Task> getTasksByType(TaskType type) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getType() == type) {
                result.add(task);
            }
        }
        return result;
    }

    public List<Task> getPendingTasks() {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                result.add(task);
            }
        }
        return result;
    }

    public List<Task> getCompletedTasks() {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isCompleted()) {
                result.add(task);
            }
        }
        return result;
    }

    public void completeTask(int taskId, List<Rank> rankDefinitions) throws TaskNotFoundException {
        Optional<Task> maybeTask = findTaskById(taskId);
        if (maybeTask.isEmpty()) {
            throw new TaskNotFoundException(taskId);
        }

        Task task = maybeTask.get();
        if (task.isCompleted()) {
            System.out.println("Task is already completed.");
            return;
        }

        task.complete();


        int basePoints = task.calculatePoints();
        int pointsEarned = basePoints;

        if (task.getDueDate() != null && task.getCompletedAt() != null) {
            LocalDate completionDate = task.getCompletedAt().toLocalDate();
            if (completionDate.isAfter(task.getDueDate())) {
                pointsEarned = basePoints / 2;
                System.out.println("Task completed after due date. Points reduced from " +
                        basePoints + " to " + pointsEarned + ".");
            }
        }

        totalPoints += pointsEarned;
        updateLevel();

        System.out.println("Task completed! You earned " + pointsEarned + " points.");
        updateStreak(task);
        updateRank(rankDefinitions);
        checkAchievementsAfterTask();
        System.out.println(MotivationalMessages.randomMessage());
    }

    private void updateLevel() {
        this.level = (totalPoints / 100) + 1;
    }

    private void updateStreak(Task task) {
        if (task.getCompletedAt() == null) {
            return;
        }
        LocalDate completionDate = task.getCompletedAt().toLocalDate();

        if (lastCompletionDate == null) {
            currentStreakDays = 1;
        } else if (completionDate.equals(lastCompletionDate)) {
            // Same day as last completion -> streak unchanged
        } else if (completionDate.equals(lastCompletionDate.plusDays(1))) {
            // Consecutive day -> streak++
            currentStreakDays++;
        } else {
            // Gap -> streak resets
            currentStreakDays = 1;
        }

        if (currentStreakDays > longestStreakDays) {
            longestStreakDays = currentStreakDays;
        }

        lastCompletionDate = completionDate;
    }

    public void updateRank(List<Rank> rankDefinitions) {
        Rank newRank = Rank.getRankForPoints(totalPoints, rankDefinitions);
        if (newRank != null && (currentRank == null || !newRank.getName().equals(currentRank.getName()))) {
            currentRank = newRank;
            System.out.println("Congratulations! You reached rank: " + currentRank.getName());
        }
    }

    public void unlockAchievement(String achievementName) {
        if (achievements.add(achievementName)) {
            System.out.println("🏆 Achievement unlocked: " + achievementName);
        }
    }

    public Set<String> getAchievements() {
        return new HashSet<>(achievements);
    }


    private void checkAchievementsAfterTask() {
        int completedCount = getCompletedTasks().size();

        // basic task count achievements
        if (completedCount == 1) {
            unlockAchievement("First Blood");
        }
        if (completedCount == 10) {
            unlockAchievement("Task Grinder (10 tasks)");
        }
        if (completedCount == 50) {
            unlockAchievement("Task Machine (50 tasks)");
        }
        if (completedCount == 100) {
            unlockAchievement("Overachiever (100 tasks)");
        }

        // category-based achievements
        int healthCompleted = countCompletedByCategory(TaskCategory.HEALTH);
        if (healthCompleted >= 10) {
            unlockAchievement("Gym Rat (10 health tasks)");
        }

        int studyCompleted = countCompletedByCategory(TaskCategory.STUDY);
        if (studyCompleted >= 10) {
            unlockAchievement("Bookworm (10 study tasks)");
        }

        int workCompleted = countCompletedByCategory(TaskCategory.WORK);
        if (workCompleted >= 10) {
            unlockAchievement("Workhorse (10 work tasks)");
        }

        // points / level-based achievements
        if (totalPoints >= 100) {
            unlockAchievement("Rookie Scorer (100 pts)");
        }
        if (totalPoints >= 500) {
            unlockAchievement("Point Hoarder (500 pts)");
        }
        if (level >= 5) {
            unlockAchievement("Level 5 Reached");
        }
        if (level >= 10) {
            unlockAchievement("Level 10 Pro");
        }

        // streak achievements
        if (longestStreakDays >= 3) {
            unlockAchievement("On Fire (3-day streak)");
        }
        if (longestStreakDays >= 7) {
            unlockAchievement("Unstoppable (7-day streak)");
        }

        // punctuality achievements
        int earlyCompletions = countCompletedBeforeDueDate();
        if (earlyCompletions >= 5) {
            unlockAchievement("Perfectionist (5 tasks before due date)");
        }

        int lateCompletions = countCompletedAfterDueDate();
        if (lateCompletions >= 5) {
            unlockAchievement("Clutch Player (5 late tasks completed)");
        }

        // time-of-day achievements
        LocalDateTime lastCompletedAt = getLastTaskCompletionTime();
        if (lastCompletedAt != null) {
            int hour = lastCompletedAt.getHour();
            if (hour < 8) {
                unlockAchievement("Early Bird (task before 8 AM)");
            }
            if (hour >= 22) {
                unlockAchievement("Night Owl (task after 10 PM)");
            }
        }

        // cleanup & habit achievements
        int overdueFixed = countCompletedAfterDueDate();
        if (overdueFixed >= 10) {
            unlockAchievement("Procrastination Destroyer (10 overdue tasks fixed)");
        }
    }

    private int countCompletedByCategory(TaskCategory category) {
        int count = 0;
        for (Task task : tasks) {
            if (task.isCompleted() && task.getCategory() == category) {
                count++;
            }
        }
        return count;
    }

    private int countCompletedBeforeDueDate() {
        int count = 0;
        for (Task task : tasks) {
            if (!task.isCompleted()) continue;
            if (task.getDueDate() == null || task.getCompletedAt() == null) continue;

            LocalDate completionDate = task.getCompletedAt().toLocalDate();
            if (!completionDate.isAfter(task.getDueDate())) {
                count++;
            }
        }
        return count;
    }

    private int countCompletedAfterDueDate() {
        int count = 0;
        for (Task task : tasks) {
            if (!task.isCompleted()) continue;
            if (task.getDueDate() == null || task.getCompletedAt() == null) continue;

            LocalDate completionDate = task.getCompletedAt().toLocalDate();
            if (completionDate.isAfter(task.getDueDate())) {
                count++;
            }
        }
        return count;
    }

    private LocalDateTime getLastTaskCompletionTime() {
        LocalDateTime latest = null;
        for (Task task : tasks) {
            if (!task.isCompleted()) continue;
            LocalDateTime completedAt = task.getCompletedAt();
            if (completedAt == null) continue;

            if (latest == null || completedAt.isAfter(latest)) {
                latest = completedAt;
            }
        }
        return latest;
    }

    public void printStats() {
        System.out.println("=== Player Stats ===");
        System.out.println("Name: " + username);
        System.out.println("ID: " + userId);
        System.out.println("Rank: " + (currentRank != null ? currentRank.getName() : "None"));
        System.out.println("Level: " + level);
        System.out.println("Points: " + totalPoints);
        System.out.println("Current Streak: " + currentStreakDays + " days");
        System.out.println("Longest Streak: " + longestStreakDays + " days");
        System.out.println("Completed Tasks: " + getCompletedTasks().size());
        System.out.println("Pending Tasks: " + getPendingTasks().size());

        System.out.println("Completed by category:");
        System.out.println(" - HEALTH: " + countCompletedByCategory(TaskCategory.HEALTH));
        System.out.println(" - STUDY : " + countCompletedByCategory(TaskCategory.STUDY));
        System.out.println(" - WORK  : " + countCompletedByCategory(TaskCategory.WORK));

        if (achievements.isEmpty()) {
            System.out.println("Achievements: None");
        } else {
            System.out.println("Achievements:");
            for (String a : achievements) {
                System.out.println(" - " + a);
            }
        }
    }
}
