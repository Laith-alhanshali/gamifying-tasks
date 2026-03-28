//package org.laith.web.controller;
//
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import org.laith.domain.enums.TaskCategory;
//import org.laith.domain.enums.TaskDifficulty;
//import org.laith.domain.enums.TaskType;
//import org.laith.persistence.Db;
//import org.laith.web.security.AuthUser;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.sql.*;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@RestController
//@RequestMapping("/tasks")
//public class TaskController {
//
//    // ---------- DTOs ----------
//
//    public record CreateTaskRequest(
//            @NotBlank String title,
//            @NotBlank String description,
//            @NotNull TaskType taskType,
//            @NotNull TaskDifficulty difficulty,
//            @NotNull TaskCategory category,
//            LocalDate dueDate
//    ) {}
//
//    public record TaskResponse(
//            int taskId,
//            int userId,
//            String title,
//            String description,
//            String taskType,
//            String difficulty,
//            String category,
//            boolean completed,
//            LocalDate dueDate,
//            LocalDateTime createdAt,
//            LocalDateTime completedAt
//    ) {}
//
//    // ---------- ENDPOINTS ----------
//
//    @GetMapping
//    public List<TaskResponse> listMyTasks(Authentication auth) throws Exception {
//        AuthUser u = (AuthUser) auth.getPrincipal();
//        int userId = u.userId();
//
//        String sql = """
//            SELECT TASK_ID, USER_ID, TITLE, DESCRIPTION, TASK_TYPE, DIFFICULTY, CATEGORY,
//                   COMPLETED, DUE_DATE, CREATED_AT, COMPLETED_AT
//            FROM TASKS
//            WHERE USER_ID = ?
//            ORDER BY TASK_ID
//        """;
//
//        List<TaskResponse> out = new ArrayList<>();
//
//        try (Connection con = Db.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setInt(1, userId);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) out.add(mapTask(rs));
//            }
//        }
//
//        return out;
//    }
//
//    @PostMapping
//    public ResponseEntity<?> createTask(Authentication auth, @Valid @RequestBody CreateTaskRequest req) throws Exception {
//        AuthUser u = (AuthUser) auth.getPrincipal();
//        int userId = u.userId();
//
//        // created_at default is SYSTIMESTAMP in DB
//        String insert = """
//            INSERT INTO TASKS (
//              TASK_ID, USER_ID, TITLE, DESCRIPTION, TASK_TYPE, DIFFICULTY, CATEGORY,
//              COMPLETED, DUE_DATE, CREATED_AT, COMPLETED_AT
//            ) VALUES (
//              TASKS_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?,
//              'N', ?, SYSTIMESTAMP, NULL
//            )
//        """;
//
//        try (Connection con = Db.getConnection()) {
//            con.setAutoCommit(false);
//
//            int newId;
//            try (PreparedStatement ps = con.prepareStatement(insert)) {
//                ps.setInt(1, userId);
//                ps.setString(2, req.title());
//                ps.setString(3, req.description());
//                ps.setString(4, req.taskType().name());
//                ps.setString(5, req.difficulty().name());
//                ps.setString(6, req.category().name());
//
//                if (req.dueDate() == null) ps.setNull(7, Types.DATE);
//                else ps.setDate(7, Date.valueOf(req.dueDate()));
//
//                ps.executeUpdate();
//            }
//
//            // Oracle-friendly way to get the ID we just generated in this session:
//            try (PreparedStatement ps = con.prepareStatement("SELECT TASKS_SEQ.CURRVAL FROM DUAL");
//                 ResultSet rs = ps.executeQuery()) {
//                rs.next();
//                newId = rs.getInt(1);
//            }
//
//            TaskResponse created = fetchTaskOwnedByUser(con, userId, newId);
//            con.commit();
//
//            if (created == null) {
//                // super unlikely, but safe
//                return ResponseEntity.internalServerError().body("Created task but could not re-load it.");
//            }
//
//            return ResponseEntity.ok(created);
//        }
//    }
//
//    @PostMapping("/{id}/complete")
//    public ResponseEntity<?> completeTask(Authentication auth, @PathVariable int id) throws Exception {
//        AuthUser u = (AuthUser) auth.getPrincipal();
//        int userId = u.userId();
//
//        // only complete if task belongs to this user
//        String sql = """
//            UPDATE TASKS
//            SET COMPLETED='Y', COMPLETED_AT=SYSTIMESTAMP
//            WHERE TASK_ID=? AND USER_ID=?
//        """;
//
//        try (Connection con = Db.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setInt(1, id);
//            ps.setInt(2, userId);
//
//            int updated = ps.executeUpdate();
//            if (updated == 0) return ResponseEntity.status(404).body("Task not found (or not yours).");
//
//            TaskResponse after = fetchTaskOwnedByUser(con, userId, id);
//            return ResponseEntity.ok(after);
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteTask(Authentication auth, @PathVariable int id) throws Exception {
//        AuthUser u = (AuthUser) auth.getPrincipal();
//        int userId = u.userId();
//
//        String sql = "DELETE FROM TASKS WHERE TASK_ID=? AND USER_ID=?";
//
//        try (Connection con = Db.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setInt(1, id);
//            ps.setInt(2, userId);
//
//            int deleted = ps.executeUpdate();
//            if (deleted == 0) return ResponseEntity.status(404).body("Task not found (or not yours).");
//
//            return ResponseEntity.ok("OK");
//        }
//    }
//
//    // ---------- HELPERS ----------
//
//    private TaskResponse fetchTaskOwnedByUser(Connection con, int userId, int taskId) throws Exception {
//        String sql = """
//            SELECT TASK_ID, USER_ID, TITLE, DESCRIPTION, TASK_TYPE, DIFFICULTY, CATEGORY,
//                   COMPLETED, DUE_DATE, CREATED_AT, COMPLETED_AT
//            FROM TASKS
//            WHERE USER_ID=? AND TASK_ID=?
//        """;
//
//        try (PreparedStatement ps = con.prepareStatement(sql)) {
//            ps.setInt(1, userId);
//            ps.setInt(2, taskId);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                if (!rs.next()) return null;
//                return mapTask(rs);
//            }
//        }
//    }
//
//    private TaskResponse mapTask(ResultSet rs) throws Exception {
//        int taskId = rs.getInt("TASK_ID");
//        int userId = rs.getInt("USER_ID");
//
//        String title = rs.getString("TITLE");
//        String description = rs.getString("DESCRIPTION");
//
//        String type = rs.getString("TASK_TYPE");
//        String difficulty = rs.getString("DIFFICULTY");
//        String category = rs.getString("CATEGORY");
//
//        boolean completed = "Y".equalsIgnoreCase(rs.getString("COMPLETED"));
//
//        Date dueD = rs.getDate("DUE_DATE");
//        LocalDate dueDate = (dueD != null) ? dueD.toLocalDate() : null;
//
//        Timestamp createdTs = rs.getTimestamp("CREATED_AT");
//        LocalDateTime createdAt = (createdTs != null) ? createdTs.toLocalDateTime() : null;
//
//        Timestamp completedTs = rs.getTimestamp("COMPLETED_AT");
//        LocalDateTime completedAt = (completedTs != null) ? completedTs.toLocalDateTime() : null;
//
//        return new TaskResponse(
//                taskId, userId, title, description,
//                type, difficulty, category,
//                completed, dueDate, createdAt, completedAt
//        );
//    }
//}
