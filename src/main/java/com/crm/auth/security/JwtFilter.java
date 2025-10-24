package com.crm.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            System.out.println("⚠️ Bo‘sh JWT token kelgan!");
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtProvider.getClaims(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            List<String> permissions = claims.get("permissions", List.class);

            // 🔹 Agar foydalanuvchi hali context’da autentifikatsiya qilinmagan bo‘lsa
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                List<GrantedAuthority> authorities = new ArrayList<>();

                // 🔸 Role qo‘shish
                if (role != null)
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

                // 🔸 Permission qo‘shish
                if (permissions != null)
                    permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));

                // 🔹 Authentication yaratish va context’ga joylash
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

                System.out.println("🟢 JWT authenticated: " + email + " → " + authorities);
            }

        } catch (Exception e) {
            System.out.println("❌ JWT parse xatolik: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
