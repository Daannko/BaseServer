package dev.dankoz.BaseServer.google.auth;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
public class GoogleUser {

    @Id
    @GeneratedValue
    private Integer id;
    private String googleID;
    private String token;
    private String refreshToken;
    private Date expires;
    @ElementCollection
    @Enumerated(EnumType.STRING)  // Store enum as stringpe_list", joinColumns = @JoinColumn(name = "user_id"))
    @CollectionTable(name = "user_scope_list", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "scope")
    private Set<ScopeType> scopes;
    
}
