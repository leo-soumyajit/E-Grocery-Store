package com.soumyajit.E_Grocery.Shop.Security;

import com.soumyajit.E_Grocery.Shop.Entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secretKey}")
    private String secretKey;

    public SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    //access token
    public String generateAccessToken(User userEntities){
        return Jwts.builder()
                .setSubject(userEntities.getId().toString())
                .claim("email",userEntities.getEmail())
                .claim("roles", userEntities.getRoles())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*10*6*24)) //24 min
                .signWith(getSecretKey())
                .compact();
    }

    //refresh Token
    public String generateRefreshToken(User userEntities){
        return Jwts.builder()
                .setSubject(userEntities.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+ 1000L *60*60*24*30*6)) //6 months
                .signWith(getSecretKey())
                .compact();
    }

    public Long getUserIdFromToken(String token){
        Claims claims =  Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }
}
