package org.laith.domain.model;

import org.laith.domain.contracts.Completable;
import org.laith.domain.contracts.Scorable;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task implements Completable, Scorable {

    private static int NEXT_ID = 1;

    private final int id;
    private String title;
    private String description;
    private TaskType type;
    private TaskDifficulty difficulty;
    private TaskCategory category;
    private boolean completed;
    private LocalDate dueDate;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private static final int DAILY_BASE_POINTS = 10;
    private static final int WEEKLY_BASE_POINTS = 30;
    private static final int MONTHLY_BASE_POINTS = 80;
    private static final int ONE_TIME_BASE_POINTS = 20;

    // Constructor for NEW tasks created in the app
    public Task(String title,
                String description,
                TaskType type,
                TaskDifficulty difficulty,
                TaskCategory category,
                LocalDate dueDate) {
        this.id = NEXT_ID++;
        this.title = title;
        this.description = description;
        this.type = type;
        this.difficulty = difficulty;
        this.category = category;
        this.dueDate = dueDate;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
        this.completedAt = null;
    }

    // Constructor for loading from file later (not used yet, but ready)
    public Task(int id,
                String title,
                String description,
                TaskType type,
                TaskDifficulty difficulty,
                TaskCategory category,
                LocalDate dueDate,
                boolean completed,
                LocalDateTime createdAt,
                LocalDateTime completedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.difficulty = difficulty;
        this.category = category;
        this.dueDate = dueDate;
        this.completed = completed;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static void setNextId(int nextId) {
        NEXT_ID = nextId;
    }

    public int getId() {
        return id;
    }

    @Override
    public void complete() {
        if (!completed) {
            this.completed = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public int calculatePoints() {
        int basePoints = switch (type) {
            case DAILY -> DAILY_BASE_POINTS;
            case WEEKLY -> WEEKLY_BASE_POINTS;
            case MONTHLY -> MONTHLY_BASE_POINTS;
            case ONE_TIME -> ONE_TIME_BASE_POINTS;
        };

        double multiplier = switch (difficulty) {
            case EASY -> 1.0;
            case MEDIUM -> 1.5;
            case HARD -> 2.0;
        };

        return (int) Math.round(basePoints * multiplier);

    }


    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !completed && dueDate.isBefore(today);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskType getType() {
        return type;
    }

    public TaskDifficulty getDifficulty() {
        return difficulty;
    }

    public TaskCategory getCategory() {
        return category;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public void setDifficulty(TaskDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setCategory(TaskCategory category) {
        this.category = category;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        String status;
        if (completed) {
            status = "COMPLETED";
        } else if (isOverdue()) {
            status = "OVERDUE";
        } else {
            status = "PENDING";
        }

        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", difficulty=" + difficulty +
                ", category=" + category +
                ", status=" + status +
                ", dueDate=" + dueDate +
                '}';
    }
}
