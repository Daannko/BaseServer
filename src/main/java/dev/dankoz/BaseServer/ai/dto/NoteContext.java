package dev.dankoz.BaseServer.ai.dto;

import java.util.Map;

/**
 * A selected board element sent as context for board chat. Geometry is optional.
 *
 * @param id      element server id
 * @param type    element type (e.g. {@code note})
 * @param content TiptapDoc rich-text body
 */
public record NoteContext(
        String id,
        String type,
        Map<String, Object> content,
        Double x,
        Double y,
        Double width,
        Double height) {

    public boolean hasGeometry() {
        return x != null && y != null;
    }
}
