package org.laith.web.controller;

import org.laith.persistence.Db;
import org.laith.web.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
public class MeController {

    public record MeProfileResponse(
            boolean authenticated,
            int userId,
            String username,
            String role,
            int totalPoints,
            int xp,
            int level,
            String rank,
            int currentStreak,
            int longestStreak,
            int tasksCompleted,
            int achievementsCount
    ) {}

    @GetMapping("/me/profile")
    public MeProfileResponse profile(Authentication auth) throws Exception {

        // ✅ SAFE: if not authenticated, return a clean JSON response
        if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof AuthUser)) {
            return new MeProfileResponse(
                    false,
                    0,
                    null,
                    null,
                    0,
                    0,
                    1,
                    "BRONZE",
                    0,
                    0,
                    0,
                    0
            );
        }

        AuthUser u = (AuthUser) auth.getPrincipal();
        int userId = u.userId();

        String userSql = """
            SELECT USER_ID, USERNAME, ROLE, TOTAL_POINTS, CURRENT_STREAK, LONGEST_STREAK, RANK_NAME
            FROM USERS
            WHERE USER_ID = ?
        """;

        int totalPoints;
        int currentStreak;
        int longestStreak;
        String username;
        String role;
        String rankName;

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(userSql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // ✅ safe: user row missing even though JWT exists
                    return new MeProfileResponse(false, userId, null, null,
                            0, 0, 1, "BRONZE", 0, 0, 0, 0);
                }

                username = rs.getString("USERNAME");
                role = rs.getString("ROLE");
                totalPoints = rs.getInt("TOTAL_POINTS");
                currentStreak = rs.getInt("CURRENT_STREAK");
                longestStreak = rs.getInt("LONGEST_STREAK");
                rankName = rs.getString("RANK_NAME");
            }
        }

        // tasks completed
        int tasksCompleted = 0;
        String completedSql = "SELECT COUNT(*) FROM TASKS WHERE USER_ID = ? AND COMPLETED = 'Y'";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(completedSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tasksCompleted = rs.getInt(1);
            }
        }

        // achievements count
        int achievementsCount = 0;
        String achSql = "SELECT COUNT(*) FROM ACHIEVEMENTS WHERE USER_ID = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(achSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) achievementsCount = rs.getInt(1);
            }
        }

        // ✅ requirement: xp = totalPoints
        int xp = totalPoints;

        // ✅ keep your current rule (stable). If later you want: match domain exactly.
        int level = Math.max(1, (totalPoints / 100) + 1);

        // ✅ keep rank as DB value if present
        String rank = (rankName == null || rankName.isBlank())
                ? "BRONZE"
                : rankName.toUpperCase();

        return new MeProfileResponse(
                true,
                userId,
                username,
                role,
                totalPoints,
                xp,
                level,
                rank,
                currentStreak,
                longestStreak,
                tasksCompleted,
                achievementsCount
        );
    }
}
