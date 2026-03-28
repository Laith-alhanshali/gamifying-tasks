package org.laith.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.laith.domain.enums.Role;
import org.laith.persistence.Db;
import org.laith.persistence.DbWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminUsersController {

    // ---------- DTOs ----------
    public record CreateUserRequest(
            @NotBlank String username,
            String password // optional: if null/blank -> user has no password yet
    ) {}

    public record UserRow(
            int userId,
            String username,
            String role,
            int totalPoints,
            int currentStreak,
            int longestStreak,
            LocalDate lastCompletionDate,
            String rankName,
            boolean hasPassword
    ) {}

    // ---------- Endpoints ----------

    // ADMIN: list all users
    // GET /admin/users
    @GetMapping
    public List<UserRow> listAllUsers() throws Exception {
        String sql = """
            SELECT USER_ID, USERNAME, ROLE, TOTAL_POINTS, CURRENT_STREAK, LONGEST_STREAK,
                   LAST_COMPLETION_DATE, RANK_NAME,
                   CASE WHEN PASSWORD_HASH IS NULL OR TRIM(PASSWORD_HASH) IS NULL THEN 0 ELSE 1 END AS HAS_PW
            FROM USERS
            ORDER BY USER_ID
        """;

        List<UserRow> out = new ArrayList<>();

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("USER_ID");
                String username = rs.getString("USERNAME");
                String role = rs.getString("ROLE");

                int points = rs.getInt("TOTAL_POINTS");
                int currentStreak = rs.getInt("CURRENT_STREAK");
                int longestStreak = rs.getInt("LONGEST_STREAK");

                Date last = rs.getDate("LAST_COMPLETION_DATE");
                LocalDate lastCompletion = (last != null) ? last.toLocalDate() : null;

                String rank = rs.getString("RANK_NAME");
                boolean hasPw = rs.getInt("HAS_PW") == 1;

                out.add(new UserRow(
                        id, username, role,
                        points, currentStreak, longestStreak,
                        lastCompletion, rank, hasPw
                ));
            }
        }

        return out;
    }

    public record ChangeRoleRequest(String role) {}

    @PatchMapping("/{id}/role")
    public void changeRole(@PathVariable int id, @RequestBody ChangeRoleRequest req) throws Exception {
        String role = req.role() == null ? "" : req.role().toUpperCase();
        if (!role.equals("ADMIN") && !role.equals("PLAYER")) {
            throw new IllegalArgumentException("Invalid role");
        }

        String sql = "UPDATE USERS SET ROLE = ? WHERE USER_ID = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }


    // ADMIN: create new player
    // POST /admin/users
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest req) throws Exception {
        // always PLAYER for this endpoint (matches your CLI "Create new player")
        int newId = DbWriter.insertUser(req.username().trim(), Role.PLAYER);

        // optional: set password (SHA-256) if provided
        if (req.password() != null && !req.password().isBlank()) {
            DbWriter.setUserPassword(newId, req.password());
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "userId", newId,
                "username", req.username().trim(),
                "role", "PLAYER",
                "passwordSet", (req.password() != null && !req.password().isBlank())
        ));
    }

    // ADMIN: reset a player's stats
    // POST /admin/users/{id}/reset
    @PostMapping("/{id}/reset")
    public ResponseEntity<?> resetUser(@PathVariable int id) throws Exception {
        DbWriter.resetUserStats(id);
        return ResponseEntity.ok(Map.of("ok", true, "resetUserId", id));
    }

    // ADMIN: delete a player
    // DELETE /admin/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) throws Exception {
        DbWriter.deleteUser(id);
        return ResponseEntity.ok(Map.of("ok", true, "deletedUserId", id));
    }
}
