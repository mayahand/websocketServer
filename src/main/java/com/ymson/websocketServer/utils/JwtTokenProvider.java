package com.ymson.websocketServer.utils;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final long TOKEN_VALID_MILISECOND = 1000L * 60 * 60;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    public String generateToken(String userId) {
        Date now = new Date();
        return Jwts.builder()
                .setId(userId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + TOKEN_VALID_MILISECOND))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return getClaims(token).getBody().getId();
    }

    private Jws<Claims> getClaims(String jwt) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
        } catch (SignatureException ex) {
            //log.error("Invalid JWT signature");
            throw ex;
        } catch (MalformedJwtException ex) {
            //log.error("Invalid JWT token");
            throw ex;
        } catch (ExpiredJwtException ex) {
            //log.error("Expired JWT token");
            throw ex;
        } catch (UnsupportedJwtException ex) {
            //log.error("Unsupported JWT token");
            throw ex;
        } catch (IllegalArgumentException ex) {
            //log.error("JWT claims string is empty.");
            throw ex;
        }
    }
}