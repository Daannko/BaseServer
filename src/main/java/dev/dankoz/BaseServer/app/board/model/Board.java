package dev.dankoz.BaseServer.app.board.model;

import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.annotation.Id;
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
    private String id;
    private String name;
    private String description;
    private Integer authorId;

    /** Canvas dimensions. */
    private Integer width;
    private Integer height;

    /** Last camera position; null until the client persists one. */
    private Camera camera;

    private Set<String> topics;

    public void addTopic(String topic){
        this.topics.add(topic);
    }

    public void removeTopic(String topic){
        this.topics.remove(topic);
    }
}
