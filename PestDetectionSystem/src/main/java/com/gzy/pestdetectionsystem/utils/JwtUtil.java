package com.gzy.pestdetectionsystem.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.secret}")
    private String secret;

    private static Key SIGNING_KEY;
    private static long EXPIRATION;

    @PostConstruct
    public void init() {
        SIGNING_KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        EXPIRATION = this.expiration;
    }

    public static String createToken(Long userId, int roleId) {
        long now = System.currentTimeMillis();

        JwtBuilder jwtBuilder = Jwts.builder();

        return jwtBuilder
                //header
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                //payload
                .setId(UUID.randomUUID().toString())//JWT ID
                .claim("role", roleId)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(now)) //签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) //过期时间
                //signature
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    //解析Token
    private static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public static int getRoleIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", Integer.class);
    }

}
