package dev.dankoz.BaseServer.app.board.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.auto.value.AutoValue.Builder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Builder
@Setter
@Getter
@NoArgsConstructor
@Document(collection = "topics")
public class Topic {

    @Id
    private Integer id;
    private String title;
    private String content;
    private Long tier;
    private Type type;
    private Long authorId;
    private Board board;

    @DBRef
    private List<Topic> relatedTopics;
    public enum Type{
        MAIN,CONNECTOR
    }

}   

