package dev.dankoz.BaseServer.app.board.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "elements")
public class ImageElement extends Element {

    private String src;      // storage-agnostic URL, never base64
    private Integer naturalWidth;
    private Integer naturalHeight;

    public ImageElement() {}

    public String getSrc() { return src; }
    public void setSrc(String src) { this.src = src; }

    public Integer getNaturalWidth() { return naturalWidth; }
    public void setNaturalWidth(Integer naturalWidth) { this.naturalWidth = naturalWidth; }

    public Integer getNaturalHeight() { return naturalHeight; }
    public void setNaturalHeight(Integer naturalHeight) { this.naturalHeight = naturalHeight; }
}
