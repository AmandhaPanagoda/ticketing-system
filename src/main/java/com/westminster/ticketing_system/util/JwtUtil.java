package com.westminster.ticketing_system.util;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.Map;
import java.security.*;
import java.util.function.Function;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    public static final String SECRET_KEY = "8889cd2b482aab6f0a64ec4af63ca999f9c8dc646afdf061f2c3cbf5bf97069cdf222e34f11e6b37f1bae2701bf40df6a85fe3cd5e4504a52df4213c58d099c8bb136ea128cff8dedf5e538d43b7390768183cf843cd77cb920f0931d011af8726820d7d3e546d2866559ddef6470e07f414782b7861b994f665913a4aa9faa23be6ce9342b34605a188fa1ae83f38c2f9728cbcfc2e1cc8cb2080786ac4828b93cdad5f6cdfd7d607663df9b08ec278db53f63e34b460f8fe3adbf4666f0dc11ea0fc7056e7c13fad07351ab9dd95b557d59fc10f0a13f208a159fc096bd95b05c478035ee66a42ec6ed933951e39b4aea73932d06c5e71f3bbe4535516cfbe";

    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 30))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}