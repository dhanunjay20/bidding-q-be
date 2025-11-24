package com.event.bidding.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173", "https://webidclient.tconsolutions.com" , "https://webidadmin.tconsolutions.com")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization", "content-type")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        // Permit all auth-related endpoints (login, forgot, reset, verify OTP, etc.)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Permit vendor/user public registration & login endpoints
                        .requestMatchers(
                                "/api/user/register",
                                "/api/vendor/register",
                                "/api/user/login",
                                "/api/vendor/login"
                        ).permitAll()

                        // Allow preflight OPTIONS requests
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // Optionally allow public GETs for user/vendor (uncomment if desired)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/user/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/vendor/**").permitAll()

                        // Allow PUT for development (change to .authenticated() later if desired)
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/user/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/vendor/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}