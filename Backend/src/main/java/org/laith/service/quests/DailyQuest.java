package org.laith.service.quests;

import org.laith.domain.enums.TaskCategory;
import org.laith.domain.model.Task;
import org.laith.domain.model.UserProfile;

public class DailyQuest {

    private final TaskCategory categoryFilter;
    private final int targetCount;
    private int currentCount;
    private boolean completed;
    private final String description;

    public DailyQuest(TaskCategory categoryFilter, int targetCount) {
        this.categoryFilter = categoryFilter;
        this.targetCount = targetCount;
        this.currentCount = 0;
        this.completed = false;
        this.description = "Complete " + targetCount + " " + categoryFilter + " tasks (daily quest)";
    }

    public void onTaskCompleted(Task task, UserProfile user) {
        if (completed) return;
        if (!task.isCompleted()) return;
        if (task.getCategory() != categoryFilter) return;

        currentCount++;
        if (currentCount >= targetCount) {
            completed = true;
            System.out.println("🎯 Daily quest completed!");
            user.unlockAchievement("Daily Quest Finisher");
        }
    }

    public String getDescription() {
        return description;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String toString() {
        return description + " | Progress: " + currentCount + "/" + targetCount +
                (completed ? " (DONE)" : "");
    }
}
