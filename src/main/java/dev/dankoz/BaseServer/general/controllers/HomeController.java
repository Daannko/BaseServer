package dev.dankoz.BaseServer.general.controllers;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class HomeController {

    @GetMapping
    public String home(Principal principal){
        return "Hello " + principal.getName();
    }

    @GetMapping("/basic")
    public String basic(){
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "{\"test\":\"elo\"}" ;
    }

    @GetMapping("/test")
    public String test(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Hello Basic" ;
    }


}
