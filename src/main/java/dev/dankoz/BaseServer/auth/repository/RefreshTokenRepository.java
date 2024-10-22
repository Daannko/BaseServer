package dev.dankoz.BaseServer.auth.repository;


import dev.dankoz.BaseServer.auth.model.RefreshToken;
import dev.dankoz.BaseServer.general.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    long countByValue(String value);

    Optional<RefreshToken> findAllByExpireAfterAndUser(Date now, User user);
    Optional<RefreshToken> findByValue(String value);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r set r.valid = false where r.value = :value")
    void revokeRequestToken(@Param(value = "value") String value);
}