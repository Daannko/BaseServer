package dev.dankoz.BaseServer.app.board.model;

import org.springframework.data.annotation.Id;

/**
 * Shared skeleton for every board element (Note, Section, Image, Drawing).
 * Subclasses carry {@code @Document(collection = "elements")} and add their
 * type-specific content fields.
 *
 * <p>Concrete subclasses: {@link NoteElement}, {@link SectionElement},
 * {@link ImageElement}, {@link DrawingElement}.
 */
public abstract class Element {

    @Id
    protected String id;

    protected String boardId;
    protected Integer authorId;

    // geometry — required on create
    protected Integer x;
    protected Integer y;
    protected Integer width;
    protected Integer height;

    // style — always returned, sensible defaults on create
    protected Integer zIndex;
    protected String bgColor;
    protected String borderColor;
    protected Integer borderWidth;

    // ── accessors ──

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBoardId() { return boardId; }
    public void setBoardId(String boardId) { this.boardId = boardId; }

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public Integer getX() { return x; }
    public void setX(Integer x) { this.x = x; }

    public Integer getY() { return y; }
    public void setY(Integer y) { this.y = y; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Integer getZIndex() { return zIndex; }
    public void setZIndex(Integer zIndex) { this.zIndex = zIndex; }

    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }

    public String getBorderColor() { return borderColor; }
    public void setBorderColor(String borderColor) { this.borderColor = borderColor; }

    public Integer getBorderWidth() { return borderWidth; }
    public void setBorderWidth(Integer borderWidth) { this.borderWidth = borderWidth; }
}
