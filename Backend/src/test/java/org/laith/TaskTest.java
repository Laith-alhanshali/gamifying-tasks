package org.laith;

import org.junit.jupiter.api.Test;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void constructor_newTask_setsFieldsAndDefaults() {
        LocalDateTime before = LocalDateTime.now();

        Task task = new Task(
                "Test Title",
                "Test Description",
                TaskType.ONE_TIME,
                TaskDifficulty.MEDIUM,
                TaskCategory.STUDY,
                LocalDate.now().plusDays(3)
        );

        LocalDateTime after = LocalDateTime.now();

        assertNotNull(task);
        assertTrue(task.getId() > 0);
        assertEquals("Test Title", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(TaskType.ONE_TIME, task.getType());
        assertEquals(TaskDifficulty.MEDIUM, task.getDifficulty());
        assertEquals(TaskCategory.STUDY, task.getCategory());
        assertEquals(LocalDate.now().plusDays(3), task.getDueDate());

        assertFalse(task.isCompleted());
        assertNull(task.getCompletedAt());
        assertNotNull(task.getCreatedAt());

        // createdAt should be between before and after (time window)
        assertTrue(!task.getCreatedAt().isBefore(before) && !task.getCreatedAt().isAfter(after));
    }

    @Test
    void constructor_loadedTask_setsExactState() {
        LocalDate due = LocalDate.now().plusDays(1);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        LocalDateTime completedAt = LocalDateTime.now().minusDays(1);

        Task task = new Task(
                77,
                "Loaded Title",
                "Loaded Desc",
                TaskType.WEEKLY,
                TaskDifficulty.HARD,
                TaskCategory.WORK,
                due,
                true,
                createdAt,
                completedAt
        );

        assertEquals(77, task.getId());
        assertEquals("Loaded Title", task.getTitle());
        assertEquals("Loaded Desc", task.getDescription());
        assertEquals(TaskType.WEEKLY, task.getType());
        assertEquals(TaskDifficulty.HARD, task.getDifficulty());
        assertEquals(TaskCategory.WORK, task.getCategory());
        assertEquals(due, task.getDueDate());
        assertTrue(task.isCompleted());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(completedAt, task.getCompletedAt());
    }

    @Test
    void calculatePoints_returnsExpectedForTypeAndDifficulty() {
        Task dailyEasy = new Task("t", "d", TaskType.DAILY, TaskDifficulty.EASY, TaskCategory.OTHER, LocalDate.now());
        Task weeklyMedium = new Task("t", "d", TaskType.WEEKLY, TaskDifficulty.MEDIUM, TaskCategory.OTHER, LocalDate.now());
        Task monthlyHard = new Task("t", "d", TaskType.MONTHLY, TaskDifficulty.HARD, TaskCategory.OTHER, LocalDate.now());

        assertEquals(10, dailyEasy.calculatePoints());          // 10 * 1.0
        assertEquals(45, weeklyMedium.calculatePoints());       // 30 * 1.5 = 45
        assertEquals(160, monthlyHard.calculatePoints());       // 80 * 2.0 = 160
    }

    @Test
    void isOverdue_trueWhenDueDateBeforeTodayAndNotCompleted() {
        Task task = new Task("t", "d", TaskType.ONE_TIME, TaskDifficulty.EASY, TaskCategory.OTHER,
                LocalDate.now().minusDays(1));

        assertFalse(task.isCompleted());
        assertTrue(task.isOverdue());

        task.complete();
        assertTrue(task.isCompleted());
        assertFalse(task.isOverdue());

    }
}
