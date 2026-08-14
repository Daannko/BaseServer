package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.Map;

/**
 * A selected note passed as generation context.
 *
 * @param id      element id (serverId if saved, else local id) — echoed into source links
 * @param content TiptapDoc rich-text body
 * @param x       world-space x (optional — older clients omit geometry)
 * @param y       world-space y (optional)
 * @param width   world-space width (optional)
 * @param height  world-space height (optional)
 */
public record NoteInput(
        String id,
        Map<String, Object> content,
        Double x,
        Double y,
        Double width,
        Double height) {

    public boolean hasGeometry() {
        return x != null && y != null;
    }
}
