package dev.dankoz.BaseServer.auth.controller;

import dev.dankoz.BaseServer.auth.dto.*;
import dev.dankoz.BaseServer.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto registerRequestDto){
        return authService.register(registerRequestDto);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto){
        return authService.token(loginRequestDto);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request){
        return authService.refresh(request);
    }
}
