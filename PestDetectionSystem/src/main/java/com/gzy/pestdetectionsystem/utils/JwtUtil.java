package com.gzy.pestdetectionsystem.utils;

import com.gzy.pestdetectionsystem.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final long EXPIRATION = 1000 * 60 * 60 * 24;
    private static final String SECRET = "SHUCOLLEGEOFCOMPUTERENGINEERINGANDSCIENCE22123131";

    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

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

