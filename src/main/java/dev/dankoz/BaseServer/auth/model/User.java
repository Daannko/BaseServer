package dev.dankoz.BaseServer.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Builder
@AllArgsConstructor
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

    public Set<Permission> getPermissions() {
        return permissions;
    }

    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Permission> permissions;

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

    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of();
    }

}
