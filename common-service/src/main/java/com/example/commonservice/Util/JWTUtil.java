package com.example.commonservice.Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

@Component
public class JWTUtil {

    private final String SECRET_KEY = "8f3ca84c5122366a19f7804e71c432fc639c7817903321a43879fe8c436c2311";

    private final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    private static final long expirationTimeMillisConst = 100000 * 60 * 24;

    public String extractUserId(String token){
        logger.info("getting user id");
        return extractClaim(token, Claims ::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateJwtToken(
            UserDetails user
    ){
        logger.info("generating token");
        return Jwts
                .builder()
                .setHeaderParam("type","JWT")
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillisConst))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails user){
        final String id = extractUserId(token);
        boolean isTokenValid = Objects.equals(id, user.getUsername()) && !isTokenExpired(token);
        logger.info("token valid: {}", isTokenValid);
        return isTokenValid;
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token,Claims :: getExpiration);
    }

    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
