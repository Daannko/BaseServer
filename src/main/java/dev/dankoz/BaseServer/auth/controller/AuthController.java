package dev.dankoz.BaseServer.auth.controller;

import dev.dankoz.BaseServer.auth.service.TokenService;
import dev.dankoz.BaseServer.auth.dto.LoginUserDto;
import dev.dankoz.BaseServer.auth.dto.RegisterUserDto;
import dev.dankoz.BaseServer.auth.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    @PostMapping("/token")
    public String token(){
        return tokenService.generateJWT("","");
    }

    @PostMapping("/register")
    public LoginUserDto register(@RequestBody RegisterUserDto registerUserDto){
        return userService.register(registerUserDto);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginUserDto loginUserDto){
        return userService.login(loginUserDto);
    }

}
