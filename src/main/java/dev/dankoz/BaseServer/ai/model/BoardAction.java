package dev.dankoz.BaseServer.ai.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A board mutation proposed by the assistant. Stored and echoed verbatim; the backend
 * never applies it — the frontend does. Only the fields relevant to {@code type} are set:
 * <ul>
 *   <li>{@code create_note} — {@code tempId}, {@code content}, {@code x}, {@code y}, {@code width}, {@code height}</li>
 *   <li>{@code update_note} — {@code id}, {@code content}</li>
 *   <li>{@code move_note}   — {@code id}, {@code x}, {@code y}</li>
 *   <li>{@code delete_note} — {@code id}</li>
 * </ul>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoardAction {

    /** create_note | update_note | move_note | delete_note. */
    private String type;

    /** create_note: client-local handle so later actions in the same response can refer to it. */
    private String tempId;

    /** Server id of an existing note (update/move/delete). */
    private String id;

    /** TiptapDoc body (create/update). */
    private Map<String, Object> content;

    private Double x;
    private Double y;
    private Double width;
    private Double height;

    /** Short human rationale shown in the UI. */
    private String reason;
}
