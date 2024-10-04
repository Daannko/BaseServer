package dev.dankoz.BaseServer.auth.repository;


import dev.dankoz.BaseServer.auth.model.RefreshToken;
import dev.dankoz.BaseServer.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    long countByValue(String value);

    Optional<RefreshToken> findAllByExpireAfterAndUser(Date now, User user);
    Optional<RefreshToken> findByValue(String value);
}