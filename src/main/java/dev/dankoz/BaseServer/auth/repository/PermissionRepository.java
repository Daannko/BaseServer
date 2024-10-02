package dev.dankoz.BaseServer.auth.repository;

import dev.dankoz.BaseServer.auth.model.Permission;
import org.springframework.data.repository.CrudRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface PermissionRepository extends CrudRepository<Permission,Integer> {
    Optional<Permission> findByName(String name);
}
