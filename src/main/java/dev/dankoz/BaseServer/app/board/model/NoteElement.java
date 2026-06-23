package dev.dankoz.BaseServer.app.board.model;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "elements")
public class NoteElement extends Element {

    private Map<String, Object> content; // TiptapDoc
    private Integer fontSize;
    private Integer padding;

    public NoteElement() {}

    public Map<String, Object> getContent() { return content; }
    public void setContent(Map<String, Object> content) { this.content = content; }

    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }

    public Integer getPadding() { return padding; }
    public void setPadding(Integer padding) { this.padding = padding; }
}
