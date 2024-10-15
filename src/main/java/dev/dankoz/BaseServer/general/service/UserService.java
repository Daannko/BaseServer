package dev.dankoz.BaseServer.general.service;

import dev.dankoz.BaseServer.general.dto.UserDataDTO;
import dev.dankoz.BaseServer.general.model.User;
import dev.dankoz.BaseServer.general.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User getUserById(Integer id){
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found!"));
    }

    public ResponseEntity<?> getUser() {
        return ResponseEntity.ok()
                .body(new UserDataDTO(getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName())));
    }

}

