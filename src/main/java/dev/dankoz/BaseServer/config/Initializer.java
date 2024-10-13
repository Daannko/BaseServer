package dev.dankoz.BaseServer.config;

import dev.dankoz.BaseServer.auth.model.Permission;
import dev.dankoz.BaseServer.auth.repository.PermissionRepository;

import dev.dankoz.BaseServer.general.model.User;
import dev.dankoz.BaseServer.general.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Component
public class Initializer {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Initializer(UserRepository userRepository, PermissionRepository permissionRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init(){


        if(userRepository.findByEmail("user@user.com").isEmpty()){
            User user = User.builder()
                    .name("User")
                    .email("user@user.com")
                    .createdAt(new Date(System.currentTimeMillis()))
                    .password(bCryptPasswordEncoder.encode("pass"))
                    .permissions(new HashSet<>(List.of(new Permission("PERMISSION_BASIC"))))
                    .enabled(true)
                    .build();

            userRepository.save(user);
        }

    }

}
