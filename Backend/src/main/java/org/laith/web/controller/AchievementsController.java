package org.laith.web.controller;

import org.laith.web.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/achievements")
public class AchievementsController {

    @GetMapping
    public List<String> myAchievements(Authentication auth) throws Exception {
        AuthUser u = (AuthUser) auth.getPrincipal();

        String sql = "SELECT ACHIEVEMENT FROM ACHIEVEMENTS WHERE USER_ID = ? ORDER BY ACHIEVEMENT";
        List<String> out = new ArrayList<>();

        try (Connection con = org.laith.persistence.Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, u.userId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String a = rs.getString(1);
                    if (a != null && !a.isBlank()) out.add(a);
                }
            }
        }

        return out;
    }
}
