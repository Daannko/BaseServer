package dev.dankoz.BaseServer.app.board.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "elements")
public class NoteElement extends Element {

    private Map<String, Object> content; // TiptapDoc
    private Integer fontSize;
    private Integer padding;

    /** General topic tags extracted from {@link #content} (AI). Persistent + reused while {@link #tagsHash} matches. */
    private List<String> tags;
    /** Hash of the plain-text content the {@link #tags} were extracted from; mismatch => tags are stale. */
    private String tagsHash;

    public NoteElement() {}

    public Map<String, Object> getContent() { return content; }
    public void setContent(Map<String, Object> content) { this.content = content; }

    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }

    public Integer getPadding() { return padding; }
    public void setPadding(Integer padding) { this.padding = padding; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @JsonIgnore
    public String getTagsHash() { return tagsHash; }
    public void setTagsHash(String tagsHash) { this.tagsHash = tagsHash; }
}
