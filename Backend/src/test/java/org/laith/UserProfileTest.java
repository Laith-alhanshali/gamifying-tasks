package org.laith;

import org.junit.jupiter.api.Test;
import org.laith.domain.enums.Role;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.Rank;
import org.laith.domain.model.Task;
import org.laith.domain.model.UserProfile;
import org.laith.exception.TaskNotFoundException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserProfileTest {

    private static List<Rank> defaultRanks() {
        return List.of(
                new Rank("Bronze", 0),
                new Rank("Silver", 100),
                new Rank("Gold", 300)
        );
    }

    @Test
    void constructor_newUser_setsDefaults() {
        Rank bronze = new Rank("Bronze", 0);

        UserProfile u = new UserProfile("Laith", bronze, Role.PLAYER);

        assertNotNull(u);
        assertTrue(u.getUserId() > 0);
        assertEquals("Laith", u.getUsername());
        assertEquals(Role.PLAYER, u.getRole());
        assertEquals(0, u.getTotalPoints());
        assertEquals(1, u.getLevel()); // (0/100)+1
        assertEquals(bronze.getName(), u.getCurrentRank().getName());
        assertEquals(0, u.getCurrentStreakDays());
        assertEquals(0, u.getLongestStreakDays());
        assertNull(u.getLastCompletionDate());
        assertTrue(u.getTasks().isEmpty());
        assertTrue(u.getAchievements().isEmpty());
    }

    @Test
    void constructor_loadedUser_setsExactState() {
        Rank silver = new Rank("Silver", 100);
        Set<String> achievements = new HashSet<>();
        achievements.add("First Blood");

        UserProfile u = new UserProfile(
                10,
                "LoadedUser",
                Role.ADMIN,
                silver,
                250,
                2,
                5,
                LocalDate.now().minusDays(1),
                achievements
        );

        assertEquals(10, u.getUserId());
        assertEquals("LoadedUser", u.getUsername());
        assertEquals(Role.ADMIN, u.getRole());
        assertEquals(250, u.getTotalPoints());
        assertEquals(3, u.getLevel()); // 250 -> (250/100)+1 = 3
        assertEquals("Silver", u.getCurrentRank().getName());
        assertEquals(2, u.getCurrentStreakDays());
        assertEquals(5, u.getLongestStreakDays());
        assertNotNull(u.getLastCompletionDate());
        assertTrue(u.getAchievements().contains("First Blood"));
    }

    @Test
    void completeTask_awardsPoints_updatesLevel_streak_and_unlocksAchievement() throws TaskNotFoundException {
        Rank bronze = new Rank("Bronze", 0);
        UserProfile u = new UserProfile("Tester", bronze, Role.PLAYER);

        // Due date in the future so points are not halved
        Task task = new Task("Read", "Read a chapter", TaskType.ONE_TIME, TaskDifficulty.EASY,
                TaskCategory.STUDY, LocalDate.now().plusDays(1));
        u.addTask(task);

        u.completeTask(task.getId(), defaultRanks());

        assertTrue(task.isCompleted());
        assertEquals(20, u.getTotalPoints()); // ONE_TIME base 20 * EASY 1.0 = 20
        assertEquals(1, u.getLevel());        // still 1 because <100
        assertEquals(1, u.getCurrentStreakDays());
        assertEquals(1, u.getLongestStreakDays());
        assertNotNull(u.getLastCompletionDate());

        // After first completion, your code unlocks "First Blood"
        assertTrue(u.getAchievements().contains("First Blood"));
    }

    @Test
    void completeTask_throwsTaskNotFound_whenInvalidId() {
        Rank bronze = new Rank("Bronze", 0);
        UserProfile u = new UserProfile("Tester", bronze, Role.PLAYER);

        assertThrows(TaskNotFoundException.class, () -> u.completeTask(9999, defaultRanks()));
    }
}
