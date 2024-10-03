package dev.dankoz.BaseServer.auth.controller;

import dev.dankoz.BaseServer.auth.dto.LoginResponseDto;
import dev.dankoz.BaseServer.auth.service.TokenService;
import dev.dankoz.BaseServer.auth.dto.LoginRequestDto;
import dev.dankoz.BaseServer.auth.dto.RegisterUserDto;
import dev.dankoz.BaseServer.auth.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/auth")
 class AuthController {

    private final TokenService tokenService;
    private final UserService userService;

    public AuthController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public LoginRequestDto register(@RequestBody RegisterUserDto registerUserDto){
        return userService.register(registerUserDto);
    }

    @PostMapping("/token")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto){
        return userService.token(loginRequestDto);
    }

}
