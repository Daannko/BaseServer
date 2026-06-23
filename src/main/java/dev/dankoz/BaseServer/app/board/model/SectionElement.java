package dev.dankoz.BaseServer.app.board.model;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "elements")
public class SectionElement extends Element {

    private Map<String, Object> title; // TiptapDoc

    public SectionElement() {}

    public Map<String, Object> getTitle() { return title; }
    public void setTitle(Map<String, Object> title) { this.title = title; }
}
