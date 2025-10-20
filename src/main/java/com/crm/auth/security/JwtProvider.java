package com.crm.auth.security;

import com.crm.common.exception.CustomException;
import com.crm.user.entity.Permission;
import com.crm.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long expiration;
    private final String jwtSecret = "mySuperSecretKey";
    public JwtProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // ✅ TOKEN YARATISH
    public String generateToken(String email, String role, Set<Permission> permissions) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("permissions", permissions.stream().map(Enum::name).toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, Jwts.SIG.HS256) // ✅ yangi versiyada Jwts.SIG ishlatiladi
                .compact();
    }

    // ✅ TOKEN TEKSHIRISH
    public Claims getClaims(String token) {
        return Jwts.parser()    // eski versiya uchun parserBuilder
                .verifyWith(secretKey) // ✅ yangilangan usul (parserBuilder o‘rniga)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration); // masalan 1 soat
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject(); // token ichidagi "subject" — bu email
        } catch (JwtException e) {
            throw new CustomException("❌ Token xato yoki muddati o‘tgan: " + e.getMessage());
        }
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new CustomException("Token expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new CustomException("Invalid token");
        }
    }

    // ✅ EMAIL O‘QISH
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ TOKEN VALIDLIGINI TEKSHIRISH
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            System.out.println("❌ JWT token invalid: " + e.getMessage());
            return false;
        }
    }
}
