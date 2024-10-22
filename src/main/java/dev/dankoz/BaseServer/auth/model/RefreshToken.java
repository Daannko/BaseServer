package dev.dankoz.BaseServer.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.dankoz.BaseServer.general.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false,unique = true)
    private String value;
    private Date expire;
    private boolean used;
    private boolean valid;

    public RefreshToken() {

    }
    @JsonIgnore
    public boolean isExpired(){
        return new Date(System.currentTimeMillis()).after(this.expire);
    }
    public void use(){
        this.used = true;
    }
}
