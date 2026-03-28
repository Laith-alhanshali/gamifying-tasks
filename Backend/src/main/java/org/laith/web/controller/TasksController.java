package org.laith.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.Rank;
import org.laith.domain.model.Task;
import org.laith.domain.model.UserProfile;
import org.laith.exception.TaskNotFoundException;
import org.laith.persistence.DbWriter;
import org.laith.web.security.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/tasks")
public class TasksController {

    // Same rank thresholds as your TaskManager
    private static final List<Rank> RANKS = List.of(
            new Rank("Bronze", 0),
            new Rank("Silver", 100),
            new Rank("Gold", 300),
            new Rank("Platinum", 700),
            new Rank("Diamond", 1500)
    );

    // -------- DTOs --------

    public record CreateTaskRequest(
            @NotBlank String title,
            @NotBlank String description,
            TaskType type,
            TaskDifficulty difficulty,
            TaskCategory category,
            String dueDate // "YYYY-MM-DD" or null
    ) {}

    public record TaskResponse(
            int taskId,
            String title,
            String description,
            String type,
            String difficulty,
            String category,
            boolean completed,
            String dueDate,
            String createdAt,
            String completedAt
    ) {}

    public record UpdateTaskRequest(
            String title,
            String description,
            TaskType type,
            TaskDifficulty difficulty,
            TaskCategory category,
            String dueDate // "YYYY-MM-DD" or null/blank
    ) {}


    // -------- Endpoints --------

    // GET /tasks -> list tasks for current user
    @GetMapping
    public List<TaskResponse> list(
            Authentication auth,
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) TaskType type,
            @RequestParam(required = false) TaskCategory category
    ) throws Exception {
        AuthUser u = (AuthUser) auth.getPrincipal();

        List<Task> tasks = loadTasksForUser(u.userId());

        // status filter
        String s = (status == null) ? "all" : status.trim().toLowerCase();
        tasks = switch (s) {
            case "all" -> tasks;
            case "pending" -> tasks.stream().filter(t -> !t.isCompleted()).toList();
            case "completed" -> tasks.stream().filter(Task::isCompleted).toList();
            case "overdue" -> tasks.stream().filter(Task::isOverdue).toList();
            default -> tasks; // if user passes weird value, just return all
        };

        // type filter (optional)
        if (type != null) {
            TaskType tt = type;
            tasks = tasks.stream().filter(t -> t.getType() == tt).toList();
        }

        // category filter (optional)
        if (category != null) {
            TaskCategory cc = category;
            tasks = tasks.stream().filter(t -> t.getCategory() == cc).toList();
        }

        List<TaskResponse> out = new ArrayList<>();
        for (Task t : tasks) out.add(toResponse(t));
        return out;
    }


    // POST /tasks -> create task for current user
    @PostMapping
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest req, Authentication auth) throws Exception {
        AuthUser u = (AuthUser) auth.getPrincipal();

        TaskType type = (req.type() != null) ? req.type() : TaskType.ONE_TIME;
        TaskDifficulty diff = (req.difficulty() != null) ? req.difficulty() : TaskDifficulty.EASY;
        TaskCategory cat = (req.category() != null) ? req.category() : TaskCategory.OTHER;
        LocalDate due = (req.dueDate() == null || req.dueDate().isBlank()) ? null : LocalDate.parse(req.dueDate());

        int newId = DbWriter.insertTask(u.userId(), req.title(), req.description(), type, diff, cat, due);

        // createdAt is generated in DB; returning "now" is ok for now
        Task t = new Task(newId, req.title(), req.description(), type, diff, cat, due, false, LocalDateTime.now(), null);
        return toResponse(t);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id,
                                    @Valid @RequestBody UpdateTaskRequest req,
                                    Authentication auth) throws Exception {
        AuthUser u = (AuthUser) auth.getPrincipal();

        // Load user's tasks so we can:
        // 1) check ownership
        // 2) keep current values if fields are missing
        // 3) apply CLI-style due-date rules when type changes
        Task existing = loadTasksForUser(u.userId()).stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);

        if (existing == null) {
            return ResponseEntity.status(404).body("Task not found");
        }

        // Keep old values when client sends null
        String newTitle = (req.title() != null && !req.title().isBlank()) ? req.title() : existing.getTitle();
        String newDesc  = (req.description() != null && !req.description().isBlank()) ? req.description() : existing.getDescription();

        TaskType newType = (req.type() != null) ? req.type() : existing.getType();
        TaskDifficulty newDiff = (req.difficulty() != null) ? req.difficulty() : existing.getDifficulty();
        TaskCategory newCat = (req.category() != null) ? req.category() : existing.getCategory();

        // Due-date rule (match your CLI behavior)
        LocalDate newDue;
        if (newType != TaskType.ONE_TIME) {
            LocalDate today = LocalDate.now();
            newDue = switch (newType) {
                case DAILY -> today;
                case WEEKLY -> today.plusWeeks(1);
                case MONTHLY -> today.plusMonths(1);
                default -> today;
            };
        } else {
            // ONE_TIME: accept provided dueDate, otherwise keep existing
            if (req.dueDate() == null || req.dueDate().isBlank()) {
                newDue = existing.getDueDate();
            } else {
                newDue = LocalDate.parse(req.dueDate().trim());
            }
        }

        // Update row (ownership-safe update)
        int rows = updateTaskOwnedByUser(
                id, u.userId(),
                newTitle, newDesc,
                newType, newDiff, newCat,
                newDue
        );

        if (rows == 0) {
            // Shouldn't happen because we already checked ownership, but safe.
            return ResponseEntity.status(404).body("Task not found");
        }

        // Return updated task (cheap version: just echo; if you want exact DB timestamps,
        // we can add a SELECT by id+user_id like in your old controller)
        Task updated = new Task(
                id,
                newTitle,
                newDesc,
                newType,
                newDiff,
                newCat,
                newDue,
                existing.isCompleted(),
                existing.getCreatedAt(),
                existing.getCompletedAt()
        );

        return ResponseEntity.ok(toResponse(updated));
    }


    // POST /tasks/{id}/complete -> completes task AND updates user stats+achievements + creates recurring next task
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable int id, Authentication auth) throws Exception {
        AuthUser authUser = (AuthUser) auth.getPrincipal();

        UserProfile user = loadUserWithTasks(authUser.userId());
        if (user == null) return ResponseEntity.status(404).body("User not found");

        // Ensure the task exists & belongs to this user
        if (user.findTaskById(id).isEmpty()) return ResponseEntity.status(404).body("Task not found");

        try {
            // 1) domain logic: marks task complete, updates points/streak/rank/achievements
            user.completeTask(id, RANKS);

            // 2) persist: mark task completed, update user stats, replace achievements
            Task completedTask = user.findTaskById(id).orElseThrow();

            if (completedTask.getCompletedAt() != null) {
                DbWriter.markTaskCompleted(completedTask.getId(), completedTask.getCompletedAt());
            }

            DbWriter.updateUserAfterProgress(user);
            DbWriter.replaceAchievements(user.getUserId(), user.getAchievements());

            // 3) create next recurring occurrence (match CLI behavior)
            TaskResponse nextRecurringTask = null;

            if (completedTask.getType() != TaskType.ONE_TIME) {
                LocalDate baseDue = (completedTask.getDueDate() != null)
                        ? completedTask.getDueDate()
                        : LocalDate.now();

                LocalDate nextDue = switch (completedTask.getType()) {
                    case DAILY -> baseDue.plusDays(1);
                    case WEEKLY -> baseDue.plusWeeks(1);
                    case MONTHLY -> baseDue.plusMonths(1);
                    default -> baseDue;
                };

                int newTaskId = DbWriter.insertTask(
                        user.getUserId(),
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

                nextRecurringTask = toResponse(next);
            }

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "taskId", id,
                    "totalPoints", user.getTotalPoints(),
                    "currentStreak", user.getCurrentStreakDays(),
                    "longestStreak", user.getLongestStreakDays(),
                    "rank", user.getCurrentRank() != null ? user.getCurrentRank().getName() : null,
                    "level", user.getLevel(),
                    "achievements", user.getAchievements(),
                    "nextRecurringTask", nextRecurringTask
            ));

        } catch (TaskNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // DELETE /tasks/{id} -> delete task if it belongs to current user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id, Authentication auth) throws Exception {
        AuthUser u = (AuthUser) auth.getPrincipal();

        int rows = deleteTaskOwnedByUser(id, u.userId());
        if (rows == 0) return ResponseEntity.status(404).body("Task not found");

        return ResponseEntity.ok(Map.of("ok", true, "deletedTaskId", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable int id, Authentication auth) throws Exception {
        AuthUser u = (AuthUser) auth.getPrincipal();

        Task t = loadTaskOwnedByUser(u.userId(), id);
        if (t == null) return ResponseEntity.status(404).body("Task not found");

        return ResponseEntity.ok(toResponse(t));
    }


    // -------- Helpers (plain JDBC) --------

    private static UserProfile loadUserWithTasks(int userId) throws Exception {
        String userSql = """
                SELECT USER_ID, USERNAME, ROLE, TOTAL_POINTS, CURRENT_STREAK, LONGEST_STREAK, LAST_COMPLETION_DATE, RANK_NAME
                FROM USERS
                WHERE USER_ID = ?
                """;

        try (Connection con = org.laith.persistence.Db.getConnection();
             PreparedStatement ps = con.prepareStatement(userSql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                int id = rs.getInt("USER_ID");
                String username = rs.getString("USERNAME");
                String roleStr = rs.getString("ROLE");
                org.laith.domain.enums.Role role = org.laith.domain.enums.Role.valueOf(roleStr.trim().toUpperCase());

                int points = rs.getInt("TOTAL_POINTS");
                int currentStreak = rs.getInt("CURRENT_STREAK");
                int longestStreak = rs.getInt("LONGEST_STREAK");

                Date last = rs.getDate("LAST_COMPLETION_DATE");
                LocalDate lastCompletion = (last != null) ? last.toLocalDate() : null;

                String rankName = rs.getString("RANK_NAME");
                Rank rank = null;
                if (rankName != null) {
                    for (Rank r : RANKS) {
                        if (r.getName().equalsIgnoreCase(rankName)) {
                            rank = r;
                            break;
                        }
                    }
                }

                Set<String> achievements = loadAchievementsForUser(id);

                UserProfile user = new UserProfile(
                        id, username, role, rank, points,
                        currentStreak, longestStreak, lastCompletion, achievements
                );

                for (Task t : loadTasksForUser(id)) user.addTask(t);

                return user;
            }
        }
    }

    private static int updateTaskOwnedByUser(int taskId,
                                             int userId,
                                             String title,
                                             String description,
                                             TaskType type,
                                             TaskDifficulty difficulty,
                                             TaskCategory category,
                                             LocalDate dueDate) throws Exception {

        // ownership-safe: only updates if the task belongs to this user
        String sql = """
        UPDATE TASKS
        SET TITLE=?, DESCRIPTION=?, TASK_TYPE=?, DIFFICULTY=?, CATEGORY=?, DUE_DATE=?
        WHERE TASK_ID=? AND USER_ID=?
    """;

        try (Connection con = org.laith.persistence.Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, type.name());
            ps.setString(4, difficulty.name());
            ps.setString(5, category.name());

            if (dueDate == null) ps.setNull(6, Types.DATE);
            else ps.setDate(6, Date.valueOf(dueDate));

            ps.setInt(7, taskId);
            ps.setInt(8, userId);

            return ps.executeUpdate();
        }
    }


    private static Set<String> loadAchievementsForUser(int userId) throws Exception {
        String sql = "SELECT ACHIEVEMENT FROM ACHIEVEMENTS WHERE USER_ID = ?";
        Set<String> set = new HashSet<>();

        try (Connection con = org.laith.persistence.Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String a = rs.getString(1);
                    if (a != null && !a.isBlank()) set.add(a);
                }
            }
        }
        return set;
    }

    private static List<Task> loadTasksForUser(int userId) throws Exception {
        String sql = """
                SELECT TASK_ID, USER_ID, TITLE, DESCRIPTION, TASK_TYPE, DIFFICULTY, CATEGORY,
                       COMPLETED, DUE_DATE, CREATED_AT, COMPLETED_AT
                FROM TASKS
                WHERE USER_ID = ?
                ORDER BY TASK_ID
                """;

        List<Task> tasks = new ArrayList<>();

        try (Connection con = org.laith.persistence.Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int taskId = rs.getInt("TASK_ID");

                    String title = rs.getString("TITLE");
                    String description = rs.getString("DESCRIPTION");

                    TaskType type = TaskType.valueOf(rs.getString("TASK_TYPE"));
                    TaskDifficulty diff = TaskDifficulty.valueOf(rs.getString("DIFFICULTY"));
                    TaskCategory cat = TaskCategory.valueOf(rs.getString("CATEGORY"));

                    boolean completed = "Y".equalsIgnoreCase(rs.getString("COMPLETED"));

                    Date dueD = rs.getDate("DUE_DATE");
                    LocalDate due = (dueD != null) ? dueD.toLocalDate() : null;

                    Timestamp createdTs = rs.getTimestamp("CREATED_AT");
                    LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : LocalDateTime.now();

                    Timestamp completedTs = rs.getTimestamp("COMPLETED_AT");
                    LocalDateTime completedAt = (completedTs != null) ? completedTs.toLocalDateTime() : null;

                    tasks.add(new Task(taskId, title, description, type, diff, cat, due, completed, createdAt, completedAt));
                }
            }
        }

        return tasks;
    }

    private static Task loadTaskOwnedByUser(int userId, int taskId) throws Exception {
        String sql = """
        SELECT TASK_ID, USER_ID, TITLE, DESCRIPTION, TASK_TYPE, DIFFICULTY, CATEGORY,
               COMPLETED, DUE_DATE, CREATED_AT, COMPLETED_AT
        FROM TASKS
        WHERE USER_ID = ? AND TASK_ID = ?
    """;

        try (Connection con = org.laith.persistence.Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, taskId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                int id = rs.getInt("TASK_ID");
                String title = rs.getString("TITLE");
                String description = rs.getString("DESCRIPTION");

                TaskType type = TaskType.valueOf(rs.getString("TASK_TYPE"));
                TaskDifficulty diff = TaskDifficulty.valueOf(rs.getString("DIFFICULTY"));
                TaskCategory cat = TaskCategory.valueOf(rs.getString("CATEGORY"));

                boolean completed = "Y".equalsIgnoreCase(rs.getString("COMPLETED"));

                Date dueD = rs.getDate("DUE_DATE");
                LocalDate due = (dueD != null) ? dueD.toLocalDate() : null;

                Timestamp createdTs = rs.getTimestamp("CREATED_AT");
                LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : LocalDateTime.now();

                Timestamp completedTs = rs.getTimestamp("COMPLETED_AT");
                LocalDateTime completedAt = (completedTs != null) ? completedTs.toLocalDateTime() : null;

                return new Task(id, title, description, type, diff, cat, due, completed, createdAt, completedAt);
            }
        }
    }


    private static int deleteTaskOwnedByUser(int taskId, int userId) throws Exception {
        String sql = "DELETE FROM TASKS WHERE TASK_ID = ? AND USER_ID = ?";

        try (Connection con = org.laith.persistence.Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }

    private static TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getType().name(),
                t.getDifficulty().name(),
                t.getCategory().name(),
                t.isCompleted(),
                t.getDueDate() != null ? t.getDueDate().toString() : null,
                t.getCreatedAt() != null ? t.getCreatedAt().toString() : null,
                t.getCompletedAt() != null ? t.getCompletedAt().toString() : null
        );
    }
}
