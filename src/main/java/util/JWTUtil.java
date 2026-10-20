package util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {
    private static final String SECRET_KEY = "9kL2mP5xR8vT1wY4zA7cE0fH3jN6qS9uX2bD5gI8lO1rU4yW7aC0eG3iM6pT9vZ";
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24小时
    
    private static SecretKey getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateToken(Long userId, String userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }
    
    public static String generateToken(Long userId, String userRole, long expirationMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("[JWT] Token expired: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.err.println("[JWT] Invalid token format: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.err.println("[JWT] Invalid signature: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[JWT] Token validation failed: " + e.getMessage());
            return false;
        }
    }
    
    public static Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String getUserRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userRole", String.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
