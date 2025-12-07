package dev.dankoz.BaseServer.app.board.model;

import java.util.Set;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "boards")
public class Board {

    @Id
    private Integer id;
    private String name;
    private String description;
    private Integer authorId;

    @DBRef  
    private Set<Topic> topics;
}
