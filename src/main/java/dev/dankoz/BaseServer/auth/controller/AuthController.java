package dev.dankoz.BaseServer.auth.controller;

import dev.dankoz.BaseServer.auth.dto.*;
import dev.dankoz.BaseServer.auth.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/auth")
 class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public LoginResponseDto register(@RequestBody RegisterRequestDto registerRequestDto){
        return userService.register(registerRequestDto);
    }

    @PostMapping("/token")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto){
        return userService.token(loginRequestDto);
    }

    @PostMapping("/refresh")
    public RefreshResponseDto login(@RequestBody RefreshRequestDto refreshRequestDto){
        return userService.refresh(refreshRequestDto);
    }

}
