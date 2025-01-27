package dev.dankoz.BaseServer.google.auth;

import dev.dankoz.BaseServer.general.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface GoogleUserRepository extends CrudRepository<GoogleUser,Integer> {

    Optional<GoogleUser> getByUser(User user);
    Optional<GoogleUser> getByUserAndGoogleMail(User user,String googleMail);
    List<GoogleUser> getAllByUser(User user);
}
