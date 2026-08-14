package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

/**
 * Request to extract (or reuse cached) general topic tags for a set of notes — the first action of
 * the "extract tags" quiz flow, callable on its own.
 *
 * @param notes source notes (only id + content are used)
 */
public record ExtractTagsRequest(List<NoteInput> notes) {
}
