package com.gzy.pestdetectionsystem.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static long EXPIRATION = 1000 * 60 * 60 * 24;
    private static final String SECRET = "SHANGHAIUNIVERSITYCOLLEGEOFCOMPUTERENGINEERINGANDSCIENCE";

    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String createToken(Long userId) {
        long now = System.currentTimeMillis();

        JwtBuilder jwtBuilder = Jwts.builder();

        return jwtBuilder
                //header
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                //payload
                .setId(UUID.randomUUID().toString())//JWT ID
                .claim("role", "user")
                .setSubject(userId.toString())
                .setIssuedAt(new Date(now)) //签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) //过期时间
                //signature
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

    }
}

