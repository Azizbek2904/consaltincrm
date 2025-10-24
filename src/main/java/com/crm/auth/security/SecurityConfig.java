package com.crm.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 🔒 CSRF o‘chiriladi (token asosida ishlaymiz)
                .csrf(csrf -> csrf.disable())

                // 🌐 CORS sozlamalari
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ⚙️ Sessiyasiz (JWT ishlatilgani uchun)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🧱 Ruxsatlar
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Ochiq endpointlar
                        .requestMatchers(
                                "/auth/login",
                                "/auth/init-super-admin",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/clients/*/files/*/preview",
                                "/clients/*/files/*/download",
                                "/api/documents"
                        ).permitAll()

                        // 🔒 Admin va Super Admin uchun
                        .requestMatchers(
                                "/api/users/**",
                                "/users/**",
                                "/api/employees/**"
                        ).hasAnyAuthority("ADMIN", "SUPER_ADMIN")

                        // 🧩 Oddiy foydalanuvchi, admin va super admin uchun
                        .requestMatchers(
                                "/api/clients/**",
                                "/api/leads/**",
                                "/api/tasks/**",
                                "/api/dashboard/**"
                        ).hasAnyAuthority("USER", "ADMIN", "SUPER_ADMIN")

                        // 🟢 OPTIONS (CORS preflight) uchun ruxsat
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🧱 Qolgan barcha so‘rovlar uchun — autentifikatsiya talab qilinadi
                        .anyRequest().authenticated()
                )

                // ⚠️ Exception handling (aniq JSON xabar bilan)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((req, res, exc) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\": \"Access Denied\"}");
                        })
                        .authenticationEntryPoint((req, res, exc) -> {
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )

                // 🔐 Custom AuthenticationProvider va JWT filter ulanishi
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🧩 Authentication provider (UserDetailsService + BCrypt)
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // 🔑 BCrypt encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🧠 AuthenticationManager (login endpoint uchun)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 🌍 To‘g‘ri CORS konfiguratsiya (Vercel + Lokal)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "https://r356453ergef.vercel.app", // Vercel frontend
                "http://localhost:5173",           // lokal dev
                "http://localhost:3030"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
