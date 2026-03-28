package org.laith.web.controller;

import org.laith.persistence.Db;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RestController
public class LeaderboardController {

    public record LeaderboardRow(
            int userId,
            String username,
            String role,
            int totalPoints,
            int xp,
            int level,
            String rank,
            int currentStreak,
            int longestStreak,
            int tasksCompleted
    ) {}

    @GetMapping("/leaderboard")
    public List<LeaderboardRow> leaderboard() throws Exception {
        String sql = """
            SELECT
              u.USER_ID,
              u.USERNAME,
              u.ROLE,
              u.TOTAL_POINTS,
              u.CURRENT_STREAK,
              u.LONGEST_STREAK,
              u.RANK_NAME,
              (SELECT COUNT(*) FROM TASKS t WHERE t.USER_ID = u.USER_ID AND t.COMPLETED = 'Y') AS TASKS_COMPLETED
            FROM USERS u
            ORDER BY u.TOTAL_POINTS DESC, u.USER_ID ASC
        """;

        List<LeaderboardRow> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int totalPoints = rs.getInt("TOTAL_POINTS");
                int xp = totalPoints; // ✅ your requirement
                int level = Math.max(1, (totalPoints / 100) + 1);

                String rankName = rs.getString("RANK_NAME");
                String rank = (rankName == null || rankName.isBlank()) ? "BRONZE" : rankName.toUpperCase();

                out.add(new LeaderboardRow(
                        rs.getInt("USER_ID"),
                        rs.getString("USERNAME"),
                        rs.getString("ROLE"),
                        totalPoints,
                        xp,
                        level,
                        rank,
                        rs.getInt("CURRENT_STREAK"),
                        rs.getInt("LONGEST_STREAK"),
                        rs.getInt("TASKS_COMPLETED")
                ));
            }
        }

        return out;
    }
}
