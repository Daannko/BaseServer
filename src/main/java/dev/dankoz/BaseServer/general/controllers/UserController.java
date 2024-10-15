package dev.dankoz.BaseServer.general.controllers;

import dev.dankoz.BaseServer.general.dto.UserDataDTO;
import dev.dankoz.BaseServer.general.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(){
       return userService.getUser();
    }

}
