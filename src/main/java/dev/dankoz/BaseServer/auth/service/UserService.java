package dev.dankoz.BaseServer.auth.service;

import dev.dankoz.BaseServer.auth.dto.LoginUserDto;
import dev.dankoz.BaseServer.auth.dto.RegisterUserDto;
import dev.dankoz.BaseServer.auth.model.Permission;
import dev.dankoz.BaseServer.auth.model.User;
import dev.dankoz.BaseServer.auth.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginUserDto register(RegisterUserDto registerUserDto) {

        User user = User.builder()
                .email(registerUserDto.email())
                .name(registerUserDto.name())
                .password(passwordEncoder.encode(registerUserDto.password()))
                .createdAt(new Date())
                .lastSeen(new Date())
                .enabled(true)
                .permissions(new HashSet<>(List.of(new Permission("BASIC"))))
                .build();

        userRepository.save(user);


        return null;
    }

    public String login(LoginUserDto loginUserDto) {

        User user = userRepository.findByEmail(loginUserDto.email())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));

        if(!passwordEncoder.matches(loginUserDto.password(),user.getPassword())){
            throw new BadCredentialsException("Invalid password!");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return tokenService.generateJWT(auth);

    }
}

