package dev.dankoz.BaseServer.todo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.dankoz.BaseServer.general.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToDo {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore()
    private User author;
    private String title;
    private String text;
    private Date created;
    private Date expires;
    private int priority;
    private String googleId;
    private boolean googleImport;

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(!(obj instanceof ToDo)){
            return false;
        }
        return id != null && id.equals(((ToDo) obj).id);
    }

    public User getAuthor() {
        return author;
    }

    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Date getCreated() {
        return created;
    }

    public Date getExpires() {
        return expires;
    }

    public int getPriority() {
        return priority;
    }

    public String getTitle(){return title;}
}
