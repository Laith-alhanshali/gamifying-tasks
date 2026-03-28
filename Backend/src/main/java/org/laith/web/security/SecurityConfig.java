package org.laith.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtService jwtService() {
        // move this to application.properties later
        return new JwtService("CHANGE_ME_TO_A_LONG_RANDOM_SECRET_32+CHARS", 60 * 60); // 1h
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // disable default auth mechanisms (we use our own)
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())

                .authorizeHttpRequests(auth -> auth
                        // allow auth endpoints explicitly
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/ping").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/leaderboard").permitAll()


                        // allow error page (otherwise you'll see confusing 403 sometimes)
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtCookieAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
