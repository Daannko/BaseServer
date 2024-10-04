package dev.dankoz.BaseServer.config;

import dev.dankoz.BaseServer.auth.model.Permission;
import dev.dankoz.BaseServer.auth.model.User;
import dev.dankoz.BaseServer.auth.repository.PermissionRepository;
import dev.dankoz.BaseServer.auth.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Component
public class Initializer {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public Initializer(UserRepository userRepository, PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init(){

        if(userRepository.findByEmail("admin@admin.com").isEmpty()){

            User user = User.builder()
                    .name("Admin")
                    .email("admin@admin.com")
                    .createdAt(new Date(System.currentTimeMillis()))
                    .password("pass")
                    .permissions(new HashSet<>(List.of(new Permission("PERMISSION_ADMIN"))))
                    .enabled(true)
                    .build();

            userRepository.save(user);
            permissionRepository.save( new Permission("PERMISSION_BASIC"));
        }

    }

}
