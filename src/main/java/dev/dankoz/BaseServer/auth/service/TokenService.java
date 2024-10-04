package dev.dankoz.BaseServer.auth.service;

import dev.dankoz.BaseServer.auth.model.RefreshToken;
import dev.dankoz.BaseServer.auth.model.User;
import dev.dankoz.BaseServer.config.RsaKeyProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Service
public class TokenService {

    private final RsaKeyProperties rsaKeyProperties;

    public TokenService(RsaKeyProperties rsaKeyProperties) {
        this.rsaKeyProperties = rsaKeyProperties;
    }

    public String generateJWT(String email,String password){
        Authentication authentication = new UsernamePasswordAuthenticationToken(email,password);
        return generateJWT(authentication);
    }
    public String generateJWT(Authentication authentication){
        Map<String,Object> claims = new HashMap<>();
        List<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        claims.put("permissions",permissions);

        long expiration = 1000 * 60  * 15; // 15 minutes
        return   Jwts.builder()
                .setClaims(claims)
                .setSubject(authentication.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(rsaKeyProperties.privateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public RefreshToken generateRefreshToken(User user){
        byte[] token = new byte[128];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(token);

        long expiration = 1000 * 60  * 60 * 24;
        return RefreshToken.builder()
                .user(user)
                .value(Base64.getEncoder().withoutPadding().encodeToString(token))
                .expire(new Date(System.currentTimeMillis() + expiration))
                .build();
    }

    public Date extractExpirationDate(String token){
        return new Date(Long.valueOf((Integer)getClaims(token).get("exp")));
    }

    public boolean isTokenExpired(String token){
        return extractExpirationDate(token).after(new Date());
    }

    public boolean isTokenValid(String token,UserDetails userDetails){
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);

    }

    public Map<String, Object> getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(rsaKeyProperties.publicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token){
        return getClaims(token).get("sub").toString();
    }



}
