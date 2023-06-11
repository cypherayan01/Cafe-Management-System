package com.inn.cafe.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    private String secret = "586E3272357538782F413F4428472D4B6150645367566B597033733676397924";

    //extracting username from the token
    public String extractUsername(String token){
        return extractClaim(token,Claims::getSubject);
    }

    //extracting all claims from the token
    public <T> T extractClaim(String token, Function<Claims,T> claimResolver){
        final Claims claims =extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    //reading claims = reading the body of a token (encrypted message)
    public Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //check if token is expired by date
    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    //extract expiration date from the token (date from java.utils)
    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }

    //generate token only from user details and no extra claims
   public String generateToken(String username,String role){
        Map<String,Object> claims = new HashMap<>();
        claims.put("role",role);
        return generateToken(claims,username);
    }

    //generating the token with the secret key
    private String generateToken(
            Map<String,Object> extraClaims,
            String subject
    ){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) //10 hours
                .signWith(getSignKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    //validation of the token
    public Boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}