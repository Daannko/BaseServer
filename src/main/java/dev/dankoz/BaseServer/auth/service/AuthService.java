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



    public ResponseEntity<?> refresh(RefreshRequestDto refreshRequestDto) {

        Optional<RefreshToken> token = refreshTokenRepository.findByValue(refreshRequestDto.refreshToken());
        RefreshToken oldRefreshToken = token.orElseThrow(() -> new RefreshTokenException("No record of token!"));
        if (oldRefreshToken.isUsed()){
            //TODO: Add some logging, this acction in prod witll mean that user was propably compromised.
            throw new RefreshTokenException("Token already used!");
        } else if (oldRefreshToken.isExpired()) {
            throw new RefreshTokenException("Token expired!");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
        User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        if(!user.getId().equals(token.get().getUser().getId())){
            throw new RefreshTokenException("Token does not belong to user!");
        }

        oldRefreshToken.use();
        refreshTokenRepository.save(oldRefreshToken);

        return getTokenResponse(user);
    }

    private ResponseEntity<?> getTokenResponse(User user){

        String jwt = tokenService.generateJWT(user);
        RefreshToken refreshToken = tokenService.generateRefreshToken(user);;

        //TODO: After localhost change change secure to true and "Lax" to "Strict"
        ResponseCookie accessTokenCookie = ResponseCookie.from("jwtToken", jwt)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(30 * 60) // 30 minutes expiration for access token
                .sameSite("Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken.getValue())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge( 24 * 60 * 60) // 7 days expiration for refresh token
                .sameSite("Lax")
                .build();

        user.addRefreshToken(refreshToken);
        userRepository.save(user);

        //TODO: REMOVE THIS, for development im resending the tokens but in future it will not be needed.
        LoginResponseDto temporary = LoginResponseDto.builder()
                .refreshToken(refreshToken.getValue())
                .jwt(jwt)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie",accessTokenCookie.toString())
                .header("Set-Cookie",refreshTokenCookie.toString())
                .body(temporary);
    }

}
