package org.laith.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.laith.util.Passwords;
import org.laith.web.security.JwtCookieAuthFilter;
import org.laith.web.security.JwtService;
import org.laith.persistence.Db;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwt;

    public AuthController(JwtService jwt) {
        this.jwt = jwt;
    }


    @GetMapping("/ping")
    public String ping() { return "pong"; }


    public record LoginRequest(@NotBlank String username, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) throws Exception {
        // 1) Look up user in DB
        DbUser dbUser = findUser(req.username());

        if (dbUser == null) return ResponseEntity.status(401).body("Invalid credentials");

        // 2) Verify password
        // If you allow NULL password_hash for some accounts, treat empty password as valid only for those.
        boolean ok;
        if (dbUser.passwordHash == null) {
            ok = (req.password() == null || req.password().isBlank());
        } else {
            ok = Passwords.verify(req.password(), dbUser.passwordHash);
        }

        if (!ok) return ResponseEntity.status(401).body("Invalid credentials");

        // 3) Create token
        String token = jwt.createToken(dbUser.userId, dbUser.username, dbUser.role);

        // 4) Set httpOnly cookie
        ResponseCookie cookie = ResponseCookie.from(JwtCookieAuthFilter.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false) // set true in HTTPS production
                .sameSite("Lax")
                .path("/")
                .maxAge(60 * 60)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body("OK");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from(JwtCookieAuthFilter.COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body("OK");
    }

    // --- DB lookup helper (minimal, uses your existing Oracle connection) ---
    private static class DbUser {
        int userId;
        String username;
        org.laith.domain.enums.Role role;
        String passwordHash;
    }

    private DbUser findUser(String username) throws Exception {
        String sql = "SELECT USER_ID, USERNAME, ROLE, PASSWORD_HASH FROM USERS WHERE LOWER(USERNAME)=LOWER(?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                DbUser u = new DbUser();
                u.userId = rs.getInt("USER_ID");
                u.username = rs.getString("USERNAME");
                u.role = org.laith.domain.enums.Role.valueOf(rs.getString("ROLE"));
                u.passwordHash = rs.getString("PASSWORD_HASH");
                return u;
            }
        }
    }
}
