package dev.dankoz.BaseServer.google.auth;

import dev.dankoz.BaseServer.general.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUser {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String googleID;
    private String googleMail;
    @Setter
    private String token;
    @Setter
    private String refreshToken;
    @Setter
    private Date expires;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_scope_list", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "scope")
    private Set<ScopeType> scopes;

    public void addScopes(Set<ScopeType> scopes){
        this.scopes.addAll(scopes);
    }

    public GoogleUserDTO getDTO(){
        return new GoogleUserDTO(this.id,this.googleMail,this.googleID,this.scopes,this.expires);
    }

}
