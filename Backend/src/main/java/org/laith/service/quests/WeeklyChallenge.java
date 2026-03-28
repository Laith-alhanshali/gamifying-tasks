package org.laith.service.quests;

import org.laith.domain.model.Task;
import org.laith.domain.model.UserProfile;

public class WeeklyChallenge {

    private final int targetCount;
    private int currentCount;
    private boolean completed;
    private final String description;

    public WeeklyChallenge(int targetCount) {
        this.targetCount = targetCount;
        this.currentCount = 0;
        this.completed = false;
        this.description = "Complete " + targetCount + " tasks (weekly challenge)";
    }

    public void onTaskCompleted(Task task, UserProfile user) {
        if (completed) return;
        if (!task.isCompleted()) return;

        currentCount++;
        if (currentCount >= targetCount) {
            completed = true;
            System.out.println("🏅 Weekly challenge completed!");
            user.unlockAchievement("Weekly Warrior");
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
