package dev.dankoz.BaseServer.auth.service;

import dev.dankoz.BaseServer.auth.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE"))
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60)) //Expires in one hour
                .subject(authentication.getName())
                .claim("scope",scope)
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }



    public Map<String, Object> getClaims(String token){
        return decoder.decode(token).getClaims();
    }

    public String extractUsername(String token){
        return getClaims(token).get("sub").toString();
    }



}
