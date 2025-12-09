package dev.dankoz.BaseServer.app.board.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "topics")
public class Topic {

    @Id
    private String id;
    private String title;
    private String content;
    private Long tier;
    private String boardId;
    private Integer authorId;

    @Builder.Default
    private Set<String> relatedTopics = new HashSet<>();

    public void addLink(String topic){
        this.relatedTopics.add(topic);
    }

    public void removeLink(String topic){
        this.relatedTopics.remove(topic);
    }

}   

