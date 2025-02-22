package dev.dankoz.BaseServer.general.model;

import dev.dankoz.BaseServer.auth.model.Permission;
import dev.dankoz.BaseServer.auth.model.RefreshToken;
import dev.dankoz.BaseServer.google.auth.GoogleUser;
import dev.dankoz.BaseServer.todo.ToDo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    @Column(nullable = false,unique = true)
    private String email;
    private String password;
    private Date createdAt;
    private boolean enabled;
    @UpdateTimestamp
    private Date lastSeen;
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private Set<RefreshToken> refreshTokens;
    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,orphanRemoval = true)
    private Set<GoogleUser> googleAccounts;

    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Permission> permissions;

    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL,fetch = FetchType.LAZY,orphanRemoval = true)
    private Set<ToDo> todos;

    public User() {

    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void addRefreshToken(RefreshToken refreshToken){
        if(this.refreshTokens == null){
            this.refreshTokens = new HashSet<>();
        }
        this.refreshTokens.add(refreshToken);
    }

    public Integer getId() {
        return id;
    }

    public Collection<? extends GrantedAuthority> getAuthorities(){
        return this.permissions.stream()
                .map(e-> new SimpleGrantedAuthority(e.getName()))
                .collect(Collectors.toList());
    }

}
