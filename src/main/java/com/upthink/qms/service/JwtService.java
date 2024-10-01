package com.upthink.qms.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import com.upthink.qms.service.request.TokenValidation;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretKey;

    private JwtService() throws NoSuchAlgorithmException{
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA512");
        SecretKey secretKeyGen = keyGen.generateKey();
        secretKey = Base64.getEncoder().encodeToString(secretKeyGen.getEncoded());
    }

    private SecretKey getKey() {
        byte[] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }

    public String generateToken(String cognitoSub, String email, List<String> userGroups) {
        // Claims mapper
        Map<String, Object> claims = new HashMap<>();
        // Add email claim
        claims.put("email", email);
        // Add cognito userGroups claims
        claims.put("cognito:groups", userGroups);

        String jwtBuilder = Jwts.builder()
                .claims()
                .add(claims)
                .subject(cognitoSub)
                .issuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24))) // 24 hours
                .and()
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
         return jwtBuilder;
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }


    public List<String> extractGroups(String token) {
        List<String> groups = extractClaim(token, claims -> claims.get("cognito:groups", List.class));
        return groups != null ? groups : Collections.emptyList();
    }

    public String extractCognitoSub(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T>T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateAuthToken(String token, UserDetails userDetails) {
        try {
            final String cognitoSub = extractCognitoSub(token);
            return (cognitoSub.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {return !isTokenExpired(token);}

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
