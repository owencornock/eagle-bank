package com.eaglebank.eaglebankapp.config;

import com.eaglebank.eaglebankapp.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtProvider;

    public SecurityConfig(JwtTokenProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    // your existing bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // new: define how HTTP security works—including JWT filter

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) turn off CSRF entirely
                .csrf(AbstractHttpConfigurer::disable)

                // 2) make the session stateless
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3) authorization rules
                .authorizeHttpRequests(auth -> auth
                        // allow signup and login anonymously
                        .requestMatchers(HttpMethod.POST, "/v1/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()

                        // allow swagger / openapi
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // everything else requires a valid JWT
                        .anyRequest().authenticated()
                )

                // 4) insert your JWT filter
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)

                // 5) (optional) keep Spring Security happy with defaults
                .httpBasic(Customizer.withDefaults())
        ;

        return http.build();
    }

    // new: a simple JWT‐parsing filter
    @Bean
    public OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                String header = req.getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    String token = header.substring(7);
                    try {
                        String userId = jwtProvider.getUserId(token);
                        var auth = new UsernamePasswordAuthenticationToken(userId, null, java.util.List.of());
                        // set the authenticated user’s ID as the “principal”
                        org.springframework.security.core.context.SecurityContextHolder
                                .getContext().setAuthentication(auth);
                    } catch (io.jsonwebtoken.JwtException e) {
                        // invalid token: we just don’t authenticate
                    }
                }
                chain.doFilter(req, res);
            }
        };
    }
}
