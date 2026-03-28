package org.laith.persistence;

import org.laith.domain.enums.Role;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.Task;
import org.laith.domain.model.UserProfile;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class DbWriter {

    // ---------- USERS (admin ops) ----------

    public static void deleteUser(int userId) throws SQLException {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                // achievements
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ACHIEVEMENTS WHERE USER_ID=?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // tasks
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM TASKS WHERE USER_ID=?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // user
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM USERS WHERE USER_ID=?")) {
                    ps.setInt(1, userId);
                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("User not found in DB (id=" + userId + ")");
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public static void resetUserStats(int userId) throws SQLException {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE USERS SET TOTAL_POINTS=0, CURRENT_STREAK=0, LONGEST_STREAK=0, " +
                                "LAST_COMPLETION_DATE=NULL, RANK_NAME=? WHERE USER_ID=?")) {
                    ps.setString(1, "Bronze");
                    ps.setInt(2, userId);
                    int affected = ps.executeUpdate();
                    if (affected == 0) throw new SQLException("User not found in DB (id=" + userId + ")");
                }


                try (PreparedStatement ps = con.prepareStatement("DELETE FROM TASKS WHERE USER_ID=?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                // clear achievements
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM ACHIEVEMENTS WHERE USER_ID=?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public static void setUserPassword(int userId, String passwordPlain) throws SQLException {
        String sql = "UPDATE USERS SET PASSWORD_HASH=? WHERE USER_ID=?";
        String hash = org.laith.security.PasswordUtil.sha256(passwordPlain);

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public static Integer authenticate(String username, String passwordPlain) throws SQLException {
        String sql = "SELECT USER_ID, PASSWORD_HASH FROM USERS WHERE LOWER(USERNAME)=LOWER(?)";
        String hash = org.laith.security.PasswordUtil.sha256(passwordPlain);

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                int userId = rs.getInt("USER_ID");
                String stored = rs.getString("PASSWORD_HASH");

                // If user existed before passwords were added, stored may be null.
                if (stored == null || stored.isBlank()) return null;

                return stored.equals(hash) ? userId : null;
            }
        }
    }

    public static boolean userHasPassword(String username) throws SQLException {
        String sql = "SELECT PASSWORD_HASH FROM USERS WHERE LOWER(USERNAME)=LOWER(?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false; // user not found
                String hash = rs.getString(1);
                return hash != null && !hash.isBlank();
            }
        }
    }

    public static void setUserPasswordByUsername(String username, String passwordPlain) throws SQLException {
        String sql = "UPDATE USERS SET PASSWORD_HASH=? WHERE LOWER(USERNAME)=LOWER(?)";
        String hash = org.laith.security.PasswordUtil.sha256(passwordPlain);

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }



    // ---------- USERS ----------

    public static int insertUser(String username, Role role) throws SQLException {
        String sql = "INSERT INTO USERS (USER_ID, USERNAME, ROLE, TOTAL_POINTS, CURRENT_STREAK, LONGEST_STREAK, " +
                "LAST_COMPLETION_DATE, RANK_NAME) " +
                "VALUES (USERS_SEQ.NEXTVAL, ?, ?, 0, 0, 0, NULL, ?)";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"USER_ID"})) {

            ps.setString(1, username);
            ps.setString(2, role.name());
            ps.setString(3, "Bronze"); // your initial rank name

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No key returned when inserting user");
                return keys.getInt(1);
            }
        }
    }

    public static void updateUserAfterProgress(UserProfile user) throws SQLException {
        String sql = "UPDATE USERS SET TOTAL_POINTS=?, CURRENT_STREAK=?, LONGEST_STREAK=?, LAST_COMPLETION_DATE=?, RANK_NAME=? " +
                "WHERE USER_ID=?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, user.getTotalPoints());
            ps.setInt(2, user.getCurrentStreakDays());
            ps.setInt(3, user.getLongestStreakDays());

            if (user.getLastCompletionDate() == null) ps.setNull(4, Types.DATE);
            else ps.setDate(4, Date.valueOf(user.getLastCompletionDate()));

            ps.setString(5, user.getCurrentRank() != null ? user.getCurrentRank().getName() : null);

            ps.setInt(6, user.getUserId());

            ps.executeUpdate();
        }
    }

    // ---------- TASKS ----------

    public static int insertTask(int userId,
                                 String title,
                                 String description,
                                 TaskType type,
                                 TaskDifficulty difficulty,
                                 TaskCategory category,
                                 LocalDate dueDate) throws SQLException {

        String sql = "INSERT INTO TASKS (TASK_ID, USER_ID, TITLE, DESCRIPTION, TASK_TYPE, DIFFICULTY, CATEGORY, COMPLETED, " +
                "DUE_DATE, CREATED_AT, COMPLETED_AT) " +
                "VALUES (TASKS_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, 'N', ?, ?, NULL)";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"TASK_ID"})) {

            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, description);

            ps.setString(4, type.name());
            ps.setString(5, difficulty.name());
            ps.setString(6, category.name());

            if (dueDate == null) ps.setNull(7, Types.DATE);
            else ps.setDate(7, Date.valueOf(dueDate));

            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No key returned when inserting task");
                return keys.getInt(1);
            }
        }
    }

    public static void updateTask(int taskId,
                                  String title,
                                  String description,
                                  TaskType type,
                                  TaskDifficulty difficulty,
                                  TaskCategory category,
                                  LocalDate dueDate) throws SQLException {

        String sql = "UPDATE TASKS SET TITLE=?, DESCRIPTION=?, TASK_TYPE=?, DIFFICULTY=?, CATEGORY=?, DUE_DATE=? WHERE TASK_ID=?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, type.name());
            ps.setString(4, difficulty.name());
            ps.setString(5, category.name());

            if (dueDate == null) ps.setNull(6, Types.DATE);
            else ps.setDate(6, Date.valueOf(dueDate));

            ps.setInt(7, taskId);

            ps.executeUpdate();
        }
    }

    public static void markTaskCompleted(int taskId, LocalDateTime completedAt) throws SQLException {
        String sql = "UPDATE TASKS SET COMPLETED='Y', COMPLETED_AT=? WHERE TASK_ID=?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(completedAt));
            ps.setInt(2, taskId);

            ps.executeUpdate();
        }
    }

    public static void deleteTask(int taskId) throws SQLException {
        String sql = "DELETE FROM TASKS WHERE TASK_ID=?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    // ---------- ACHIEVEMENTS ----------

    public static void replaceAchievements(int userId, Set<String> achievements) throws SQLException {
        try (Connection con = Db.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement del = con.prepareStatement("DELETE FROM ACHIEVEMENTS WHERE USER_ID=?")) {
                    del.setInt(1, userId);
                    del.executeUpdate();
                }

                if (achievements != null && !achievements.isEmpty()) {
                    try (PreparedStatement ins = con.prepareStatement(
                            "INSERT INTO ACHIEVEMENTS (USER_ID, ACHIEVEMENT) VALUES (?, ?)")) {

                        for (String a : achievements) {
                            ins.setInt(1, userId);
                            ins.setString(2, a);
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}
