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
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login",
                                "/auth/init-super-admin",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/clients/*/files/*/preview",
                                "/clients/*/files/*/download",
                                "/api/documents"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
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
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
   //vercel code
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
 //Azizbek
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // ✅ Frontendlarga ruxsat (Vercel + lokal)
        config.setAllowedOrigins(List.of(
                "https://r356453ergef.vercel.app", // Vercel frontend domeni
                "http://localhost:5173",          // Vite (development)
                "http://localhost:3030"           // React (development)
        ));
       //jdowjdwiedfjeidjwidjeifjedwisjdcnvfbhfeudhwjdhwinhswd
        // ✅ So‘rov turlari
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ Headerlar
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));

        // ✅ Token yoki cookie yuborishga ruxsat
        config.setAllowCredentials(true);

        // ✅ Ba’zi headerlarni frontendga ko‘rsatish
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

        // 🔥 CORS sozlamasini butun API bo‘ylab qo‘llash
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
