package org.laith;

import org.junit.jupiter.api.Test;
import org.laith.domain.enums.Role;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.service.quests.DailyQuest;
import org.laith.domain.model.Rank;
import org.laith.domain.model.Task;
import org.laith.domain.model.UserProfile;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DailyQuestTest {

    @Test
    void constructor_setsDescriptionAndDefaults() {
        DailyQuest q = new DailyQuest(TaskCategory.HEALTH, 3);

        assertNotNull(q);
        assertEquals(3, q.getTargetCount());
        assertEquals(0, q.getCurrentCount());
        assertFalse(q.isCompleted());
        assertNotNull(q.getDescription());
        assertTrue(q.toString().contains("Progress"));
    }

    @Test
    void onTaskCompleted_countsOnlyMatchingCategory_andCompletesQuest() {
        UserProfile user = new UserProfile("U", new Rank("Bronze", 0), Role.PLAYER);
        DailyQuest quest = new DailyQuest(TaskCategory.HEALTH, 2);

        Task t1 = new Task("Gym", "Workout", TaskType.ONE_TIME, TaskDifficulty.EASY,
                TaskCategory.HEALTH, LocalDate.now().plusDays(1));
        t1.complete();

        Task t2 = new Task("Run", "Cardio", TaskType.ONE_TIME, TaskDifficulty.EASY,
                TaskCategory.HEALTH, LocalDate.now().plusDays(1));
        t2.complete();

        quest.onTaskCompleted(t1, user);
        assertEquals(1, quest.getCurrentCount());
        assertFalse(quest.isCompleted());

        quest.onTaskCompleted(t2, user);
        assertEquals(2, quest.getCurrentCount());
        assertTrue(quest.isCompleted());

        assertTrue(user.getAchievements().contains("Daily Quest Finisher"));
    }

    @Test
    void onTaskCompleted_ignoresWrongCategoryOrNotCompleted() {
        UserProfile user = new UserProfile("U", new Rank("Bronze", 0), Role.PLAYER);
        DailyQuest quest = new DailyQuest(TaskCategory.HEALTH, 1);

        Task wrongCat = new Task("Study", "Read", TaskType.ONE_TIME, TaskDifficulty.EASY,
                TaskCategory.STUDY, LocalDate.now().plusDays(1));
        wrongCat.complete();

        Task notCompleted = new Task("Gym", "Workout", TaskType.ONE_TIME, TaskDifficulty.EASY,
                TaskCategory.HEALTH, LocalDate.now().plusDays(1));
        // notCompleted.complete() not called

        quest.onTaskCompleted(wrongCat, user);
        assertEquals(0, quest.getCurrentCount());
        assertFalse(quest.isCompleted());

        quest.onTaskCompleted(notCompleted, user);
        assertEquals(0, quest.getCurrentCount());
        assertFalse(quest.isCompleted());
    }
}
