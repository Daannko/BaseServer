package dev.dankoz.BaseServer.auth.service;

import dev.dankoz.BaseServer.auth.dto.LoginRequestDto;
import dev.dankoz.BaseServer.auth.dto.LoginResponseDto;
import dev.dankoz.BaseServer.auth.dto.RegisterUserDto;
import dev.dankoz.BaseServer.auth.model.Permission;
import dev.dankoz.BaseServer.auth.model.RefreshToken;
import dev.dankoz.BaseServer.auth.model.User;
import dev.dankoz.BaseServer.auth.repository.RefreshTokenRepository;
import dev.dankoz.BaseServer.auth.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.sql.Ref;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, AuthenticationManager authenticationManager, HandlerExceptionResolver handlerExceptionResolver,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public LoginRequestDto register(RegisterUserDto registerUserDto) {

        User user = User.builder()
                .email(registerUserDto.email())
                .name(registerUserDto.name())
                .password(passwordEncoder.encode(registerUserDto.password()))
                .createdAt(new Date())
                .lastSeen(new Date())
                .enabled(true)
                .permissions(new HashSet<>(List.of(new Permission("PERMISSION_BASIC"))))
                .build();

        userRepository.save(user);
        return null;
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

}

