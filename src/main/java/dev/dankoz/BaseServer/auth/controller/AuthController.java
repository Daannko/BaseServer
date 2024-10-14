package dev.dankoz.BaseServer.auth.controller;

import dev.dankoz.BaseServer.auth.dto.*;
import dev.dankoz.BaseServer.auth.service.AuthService;
import dev.dankoz.BaseServer.general.dto.UserDataDTO;
import dev.dankoz.BaseServer.general.service.UserService;
import org.springframework.web.bind.annotation.*;


@CrossOrigin
@RestController()
@RequestMapping("/auth")
 class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public LoginResponseDto register(@RequestBody RegisterRequestDto registerRequestDto){
        return authService.register(registerRequestDto);
    }
    @PostMapping("/token")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto){
        return authService.token(loginRequestDto);
    }

    @PostMapping("/refresh")
    public LoginResponseDto refresh(@RequestBody RefreshRequestDto refreshRequestDto){
        return authService.refresh(refreshRequestDto);
    }
}
