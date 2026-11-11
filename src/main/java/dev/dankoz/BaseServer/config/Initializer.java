package dev.dankoz.BaseServer.config;

import dev.dankoz.BaseServer.auth.model.Permission;
import dev.dankoz.BaseServer.auth.repository.PermissionRepository;

import dev.dankoz.BaseServer.general.model.User;
import dev.dankoz.BaseServer.general.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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
    private final PermissionRepository permissionRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${app.seed.password}")
    private String seedPassword;

    public Initializer(UserRepository userRepository, PermissionRepository permissionRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init(){

        Permission permission = permissionRepository.findByName(Permissions.PERMISSION_BASIC.getName())
                .orElseGet(() -> permissionRepository.save(new Permission(Permissions.PERMISSION_BASIC.getName())));

        if(!seedPassword.isBlank() && userRepository.findByEmail("user@user.com").isEmpty()){
            User user = User.builder()
                    .name("User")
                    .email("user@user.com")
                    .createdAt(new Date(System.currentTimeMillis()))
                    .password(bCryptPasswordEncoder.encode(seedPassword))
                    .permissions(new HashSet<>(List.of(permission)))
                    .enabled(true)
                    .build();

            userRepository.save(user);
        }

    }

}
