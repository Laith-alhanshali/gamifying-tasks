package org.laith.persistence;

import org.laith.domain.enums.Role;
import org.laith.domain.enums.TaskCategory;
import org.laith.domain.enums.TaskDifficulty;
import org.laith.domain.enums.TaskType;
import org.laith.domain.model.*;
import org.laith.service.Leaderboard;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DbStorage {

    // ---------- LOAD ----------

    public static void loadData(List<UserProfile> users,
                                Leaderboard leaderboard,
                                List<Rank> rankDefinitions) throws SQLException {

        users.clear();
        leaderboard.clear();

        Map<Integer, UserProfile> usersById = loadUsers(users, leaderboard, rankDefinitions);
        loadTasks(usersById);

        // keep NEXT_IDs safe when mixing in-memory with DB ids
        int maxUserId = usersById.keySet().stream().mapToInt(i -> i).max().orElse(0);
        UserProfile.setNextId(maxUserId + 1);

        int maxTaskId = findMaxTaskId();
        Task.setNextId(maxTaskId + 1);
    }

    private static Map<Integer, UserProfile> loadUsers(List<UserProfile> users,
                                                       Leaderboard leaderboard,
                                                       List<Rank> rankDefinitions) throws SQLException {

        Map<String, Rank> rankByName = new HashMap<>();
        for (Rank r : rankDefinitions) rankByName.put(r.getName(), r);

        String sql = """
            SELECT USER_ID, USERNAME, ROLE, TOTAL_POINTS, CURRENT_STREAK, LONGEST_STREAK,
                   LAST_COMPLETION_DATE, RANK_NAME
            FROM USERS
            ORDER BY USER_ID
        """;

        Map<Integer, UserProfile> map = new HashMap<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("USER_ID");
                String username = rs.getString("USERNAME");
                String roleStr = rs.getString("ROLE");
                Role role = (roleStr == null) ? Role.PLAYER : Role.valueOf(roleStr.trim().toUpperCase());


                int points = rs.getInt("TOTAL_POINTS");
                int currentStreak = rs.getInt("CURRENT_STREAK");
                int longestStreak = rs.getInt("LONGEST_STREAK");

                Date last = rs.getDate("LAST_COMPLETION_DATE");
                LocalDate lastCompletion = (last != null) ? last.toLocalDate() : null;

                String rankName = rs.getString("RANK_NAME");
                Rank rank = (rankName != null) ? rankByName.get(rankName) : null;

                // achievements: load from ACHIEVEMENTS table
                Set<String> achievements = loadAchievementsForUser(id);

                UserProfile u = new UserProfile(
                        id, username, role, rank, points,
                        currentStreak, longestStreak, lastCompletion, achievements
                );

                users.add(u);
                leaderboard.addUser(u);
                map.put(id, u);
            }
        }

        return map;
    }

    private static Set<String> loadAchievementsForUser(int userId) throws SQLException {
        String sql = "SELECT ACHIEVEMENT FROM ACHIEVEMENTS WHERE USER_ID = ?";
        Set<String> set = new HashSet<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(rs.getString(1));
            }
        }
        return set;
    }

    private static void loadTasks(Map<Integer, UserProfile> usersById) throws SQLException {
        String sql = """
            SELECT TASK_ID, USER_ID, TITLE, DESCRIPTION, TASK_TYPE, DIFFICULTY, CATEGORY,
                   COMPLETED, DUE_DATE, CREATED_AT, COMPLETED_AT
            FROM TASKS
            ORDER BY TASK_ID
        """;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int taskId = rs.getInt("TASK_ID");
                int userId = rs.getInt("USER_ID");

                UserProfile owner = usersById.get(userId);
                if (owner == null) continue;

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

                Task task = new Task(taskId, title, description, type, diff, cat, due, completed, createdAt, completedAt);
                owner.addTask(task);
            }
        }
    }

    private static int findMaxTaskId() throws SQLException {
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT NVL(MAX(TASK_ID), 0) FROM TASKS");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ---------- SAVE (minimal: overwrite strategy) ----------
    // For requirement 7, we’ll mainly do INSERT/UPDATE methods in TaskManager instead of full overwrite.
    // But here’s a helper you can use later if you want.

}
