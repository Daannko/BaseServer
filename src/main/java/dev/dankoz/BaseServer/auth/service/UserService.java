package dev.dankoz.BaseServer.auth.service;

import dev.dankoz.BaseServer.auth.dto.*;
import dev.dankoz.BaseServer.auth.model.Permission;
import dev.dankoz.BaseServer.auth.model.RefreshToken;
import dev.dankoz.BaseServer.auth.model.User;
import dev.dankoz.BaseServer.auth.repository.PermissionRepository;
import dev.dankoz.BaseServer.auth.repository.RefreshTokenRepository;
import dev.dankoz.BaseServer.auth.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PermissionRepository permissionRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, AuthenticationManager authenticationManager,
                       RefreshTokenRepository refreshTokenRepository,
                       PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.permissionRepository = permissionRepository;
    }

    public LoginResponseDto register(RegisterRequestDto registerRequestDto) {


        if(userRepository.countByEmail(registerRequestDto.email()) != 0){
            throw new EntityExistsException("User with this email already Exists");
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

    public LoginResponseDto token(LoginRequestDto loginRequestDto) {
        Authentication auth = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.password()));

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found!") );

        String jwt = tokenService.generateJWT(auth);

        RefreshToken refreshToken = null;

        // If there is a token and its not expired return it
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findAllByExpireAfterAndUser(new Date(System.currentTimeMillis()),user);
        if(optionalRefreshToken.isPresent()) {
            refreshToken = optionalRefreshToken.get();
        }else {
            // Make sure that there is no token with the same value already!
            while(refreshToken == null || refreshTokenRepository.countByValue(refreshToken.getValue()) != 0) {
                refreshToken = tokenService.generateRefreshToken(user);
            }
        }

        user.addRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponseDto(jwt,refreshToken.getValue());
    }

    public User getUserById(Integer id){
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));
    }

    public RefreshResponseDto refresh(RefreshRequestDto refreshRequestDto) {
        Optional<RefreshToken> token = refreshTokenRepository.findByValue(refreshRequestDto.refreshToken());

        if(token.isEmpty()){
            throw new NoSuchElementException("No record of token!");
        }

        return new RefreshResponseDto(tokenService.generateJWT(token.get().getUser()));
    }
}

