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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Ref;
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

    public LoginResponseDto register(RegisterRequestDto registerRequestDto) {

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

    public LoginResponseDto token(LoginRequestDto loginRequestDto) {
        Authentication auth;
        try{
            auth = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(loginRequestDto.email().toLowerCase(), loginRequestDto.password()));
        }
        catch (Exception e){
            throw new BadCredentialsException("Invalid email or password! :(");
        }

        User user = userRepository.findByEmail(loginRequestDto.email().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!") );

        String jwt = tokenService.generateJWT(auth);
        RefreshToken refreshToken = tokenService.generateRefreshToken(user);;

        user.addRefreshToken(refreshToken);
        userRepository.save(user);

        return new LoginResponseDto(jwt,refreshToken.getValue());
    }



    public LoginResponseDto refresh(RefreshRequestDto refreshRequestDto) {

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

        RefreshToken newRefreshToken = tokenService.generateRefreshToken(user);
        user.addRefreshToken(newRefreshToken);
        oldRefreshToken.use();

        userRepository.save(user);
        refreshTokenRepository.save(oldRefreshToken);

        return LoginResponseDto.builder()
                .jwt(tokenService.generateJWT(authentication))
                .refreshToken(newRefreshToken.getValue())
                .build();
    }


}
