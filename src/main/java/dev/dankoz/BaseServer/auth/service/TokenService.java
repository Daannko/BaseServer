package dev.dankoz.BaseServer.auth.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    public TokenService(JwtEncoder encoder, JwtDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public String generateJWT(String email,String password){
        Authentication authentication = new UsernamePasswordAuthenticationToken(email,password);
        return generateJWT(authentication);
    }
    public String generateJWT(Authentication authentication){
        Instant now = Instant.now();
        String permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60)) //Expires in one hour
                .subject(authentication.getName())
                .claim("permissions",permissions)
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Date extractExpirationDate(String token){
        return new Date((long)getClaims(token).get("exp"));
    }

    public boolean isTokenExpired(String token){
        return extractExpirationDate(token).after(new Date());
    }

    public boolean isTokenValid(String token,UserDetails userDetails){
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);

    }

    public Map<String, Object> getClaims(String token){
        return decoder.decode(token).getClaims();
    }

    public String extractUsername(String token){
        return getClaims(token).get("sub").toString();
    }



}
