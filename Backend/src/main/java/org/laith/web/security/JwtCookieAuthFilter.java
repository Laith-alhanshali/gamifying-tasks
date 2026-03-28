package org.laith.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtCookieAuthFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "access_token";

    private final JwtService jwt;

    public JwtCookieAuthFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String token = readCookie(req, COOKIE_NAME);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Jws<Claims> parsed = jwt.parse(token);
                Claims c = parsed.getBody();

                int userId = Integer.parseInt(c.getSubject());
                String username = c.get("username", String.class);
                String role = c.get("role", String.class);

                var auth = new UsernamePasswordAuthenticationToken(
                        new AuthUser(userId, username, role),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // invalid/expired token -> treat as not logged in
            }
        }

        chain.doFilter(req, res);
    }

    private String readCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
