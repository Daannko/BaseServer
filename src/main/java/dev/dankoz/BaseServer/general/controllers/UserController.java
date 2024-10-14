package dev.dankoz.BaseServer.general.controllers;

import dev.dankoz.BaseServer.general.dto.UserDataDTO;
import dev.dankoz.BaseServer.general.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDataDTO getMe(){
       return userService.getUser();
    }

}
