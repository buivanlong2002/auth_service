package com.example.auth_service.components;

import com.example.auth_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.expiration:3600}")
    private int expiration;

    @Value("${jwt.secretKey}")
    private String secretKey;

    /**
     * Tạo JWT token từ thông tin người dùng.
     */
    public String generateToken(User user) throws Exception {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("name", user.getName());
        claims.put("email", user.getEmail());
        claims.put("image", user.getAvatarUrl());

        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getEmail())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating token: {}", e.getMessage());
            throw new Exception("Error generating token: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy Key từ chuỗi secret key (chuỗi bình thường, không mã hóa).
     */
    public Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Trích xuất toàn bộ claims từ token.
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Error extracting claims from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token");
        }
    }

    /**
     * Trích xuất một claim cụ thể từ token.
     */
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Kiểm tra token có hết hạn không.
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractClaims(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    /**
     * Lấy email từ token.
     */
    public String extractUsername(String token) {
        try {
            return extractClaims(token, Claims::getSubject); // Lấy subject từ payload của token
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra token có hợp lệ không dựa trên email và hạn dùng.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Tạo secret key ngẫu nhiên (chỉ dùng khi cần tạo mới).
     */
    private String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        return java.util.Base64.getEncoder().encodeToString(keyBytes);
    }
}
