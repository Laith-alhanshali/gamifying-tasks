package org.laith;

import org.junit.jupiter.api.Test;
import org.laith.domain.enums.Role;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.Rank;
import org.laith.domain.model.Task;
import org.laith.domain.model.UserProfile;
import org.laith.service.quests.WeeklyChallenge;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class WeeklyChallengeTest {

    @Test
    void constructor_setsDescriptionAndDefaults() {
        WeeklyChallenge w = new WeeklyChallenge(5);

        assertNotNull(w);
        assertEquals(5, w.getTargetCount());
        assertEquals(0, w.getCurrentCount());
        assertFalse(w.isCompleted());
        assertNotNull(w.getDescription());
        assertTrue(w.toString().contains("Progress"));
    }

    @Test
    void onTaskCompleted_countsAnyCompletedTask_andUnlocksAchievementAtTarget() {
        UserProfile user = new UserProfile("U", new Rank("Bronze", 0), Role.PLAYER);
        WeeklyChallenge challenge = new WeeklyChallenge(2);

        Task t1 = new Task("A", "B", TaskType.ONE_TIME, TaskDifficulty.EASY, TaskCategory.OTHER, LocalDate.now());
        t1.complete();

        Task t2 = new Task("C", "D", TaskType.ONE_TIME, TaskDifficulty.EASY, TaskCategory.OTHER, LocalDate.now());
        t2.complete();

        challenge.onTaskCompleted(t1, user);
        assertEquals(1, challenge.getCurrentCount());
        assertFalse(challenge.isCompleted());

        challenge.onTaskCompleted(t2, user);
        assertEquals(2, challenge.getCurrentCount());
        assertTrue(challenge.isCompleted());

        assertTrue(user.getAchievements().contains("Weekly Warrior"));
    }

    @Test
    void onTaskCompleted_ignoresNotCompletedTasks() {
        UserProfile user = new UserProfile("U", new Rank("Bronze", 0), Role.PLAYER);
        WeeklyChallenge challenge = new WeeklyChallenge(1);

        Task t = new Task("A", "B", TaskType.ONE_TIME, TaskDifficulty.EASY, TaskCategory.OTHER, LocalDate.now());

        challenge.onTaskCompleted(t, user);
        assertEquals(0, challenge.getCurrentCount());
        assertFalse(challenge.isCompleted());
        assertFalse(user.getAchievements().contains("Weekly Warrior"));
    }
}
