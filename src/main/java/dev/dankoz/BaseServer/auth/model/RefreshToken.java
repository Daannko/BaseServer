package dev.dankoz.BaseServer.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false,unique = true)
    private String value;
    private Date expire;

    public RefreshToken() {

    }

    public boolean isExpired(){
        return new Date(System.currentTimeMillis()).after(this.expire);
    }
}
