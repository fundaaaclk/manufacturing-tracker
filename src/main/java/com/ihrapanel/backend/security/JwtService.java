package com.ihrapanel.backend.security;

import com.ihrapanel.backend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Login başarılı olduğunda kullanıcı için JWT token üretir.
    public String generateToken(User user) {

        long expirationTime = 1000 * 60 * 60 * 24; // 24 saat

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("companyId", user.getCompany().getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }
  // Token içindeki bütün claimleri okur.
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Token içindeki user UUID'sini döndürür.
    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }
     // Token içindeki email bilgisini döndürür.
    public String extractEmail(String token) {
        return extractClaims(token)
                .get("email", String.class);
    }
     // Token içindeki company UUID'sini döndürür.
    public String extractCompanyId(String token) {
        return extractClaims(token)
                .get("companyId", String.class);
    }
 // Token içindeki rolü döndürür.
    public String extractRole(String token) {
        return extractClaims(token)
                .get("role", String.class);
    }

    
     public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);

            return claims.getExpiration()
                    .after(new Date());

        } catch (Exception e) {
            return false;
        } 
 } 
}