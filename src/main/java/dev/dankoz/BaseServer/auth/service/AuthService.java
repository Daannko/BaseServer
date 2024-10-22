package dev.dankoz.BaseServer.auth.service;

import dev.dankoz.BaseServer.auth.dto.*;
import dev.dankoz.BaseServer.auth.model.Permission;
import dev.dankoz.BaseServer.auth.model.RefreshToken;
import dev.dankoz.BaseServer.auth.repository.PermissionRepository;
import dev.dankoz.BaseServer.auth.repository.RefreshTokenRepository;
import dev.dankoz.BaseServer.config.exceptions.EmailAlreadyExistsException;
import dev.dankoz.BaseServer.config.exceptions.RefreshTokenException;
import dev.dankoz.BaseServer.general.model.User;
import dev.dankoz.BaseServer.general.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PermissionRepository permissionRepository;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, AuthenticationManager authenticationManager, RefreshTokenRepository refreshTokenRepository, PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.permissionRepository = permissionRepository;
    }

    public ResponseEntity<?> register(RegisterRequestDto registerRequestDto) {

        if(userRepository.countByEmail(registerRequestDto.email()) != 0){
            throw new EmailAlreadyExistsException("User with this email already exists");
        }

        Permission permission = permissionRepository.findByName("PERMISSION_BASIC")
                .orElseThrow();

        User user = User.builder()
                .email(registerRequestDto.email().toLowerCase())
                .name(registerRequestDto.name())
                .password(passwordEncoder.encode(registerRequestDto.password()))
                .createdAt(new Date())
                .lastSeen(new Date())
                .enabled(true)
                .permissions(new HashSet<>(List.of(permission)))
                .build();

        userRepository.save(user);

        return token(new LoginRequestDto(registerRequestDto.email(), registerRequestDto.password()));
    }

    public ResponseEntity<?> token(LoginRequestDto loginRequestDto) {
        Authentication auth;
        try{
            auth = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(loginRequestDto.email().toLowerCase(), loginRequestDto.password()));
        }
        catch (Exception e){
            throw new BadCredentialsException("Invalid email or password! :(");
        }

        User user = userRepository.findByEmail(auth.getName().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!") );


        return getTokenResponse(user);
    }



    public ResponseEntity<?> refresh(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            throw new RefreshTokenException("No refresh token found");
        }

        Optional<Cookie> cookie = Arrays.stream(cookies).filter(e -> e.getName().equals("refreshToken")).findFirst();
        if(cookie.isEmpty()){
            throw new RefreshTokenException("No refresh token found");
        }

        RefreshToken oldRefreshToken = refreshTokenRepository.findByValue(cookie.get().getValue())
                .orElseThrow(() -> new RefreshTokenException("No record of token!"));

        if(!oldRefreshToken.isValid()){
            throw new RefreshTokenException("Token in no longer valid!");
        } else if (oldRefreshToken.isUsed()){
            //TODO: Add some logging, this acction in prod witll mean that user was propably compromised.
            throw new RefreshTokenException("Token already used!");
        } else if (oldRefreshToken.isExpired()) {
            throw new RefreshTokenException("Token expired!");
        }

        oldRefreshToken.use();
        refreshTokenRepository.save(oldRefreshToken);

        return getTokenResponse(oldRefreshToken.getUser());
    }

    private ResponseEntity<Void> getTokenResponse(User user){

        String jwt = tokenService.generateJWT(user);
        RefreshToken refreshToken = tokenService.generateRefreshToken(user);

        //TODO: After localhost change change secure to true and "Lax" to "Strict"
        ResponseCookie accessTokenCookie = ResponseCookie.from("jwtToken", jwt)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .sameSite("Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken.getValue())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge( Duration.ofDays(1))
                .sameSite("Lax")
                .build();

        user.addRefreshToken(refreshToken);
        userRepository.save(user);

        return ResponseEntity.noContent()
                .header("Set-Cookie",accessTokenCookie.toString())
                .header("Set-Cookie",refreshTokenCookie.toString())
                .build();
    }

    public ResponseEntity<?> logout(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            Optional<Cookie> cookie = Arrays.stream(cookies).filter(e -> e.getName().equals("refreshToken")).findFirst();
            cookie.ifPresent(value -> this.refreshTokenRepository.revokeRequestToken(value.getValue()));
        }

        ResponseCookie accessTokenCookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge( Duration.ofSeconds(5))
                .sameSite("Lax")
                .build();
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge( Duration.ofSeconds(5))
                .sameSite("Lax")
                .build();

        return ResponseEntity.noContent()
              .header("Set-Cookie",accessTokenCookie.toString())
              .header("Set-Cookie",refreshTokenCookie.toString())
              .build();
    }
}
