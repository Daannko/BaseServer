package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;
import java.util.Map;

/**
 * Result of tag extraction.
 *
 * @param noteTags per-note tags, keyed by note id (also persisted on each note)
 * @param tags     de-duplicated union of all note tags — the set a quiz would be generated from
 */
public record ExtractTagsResponse(Map<String, List<String>> noteTags, List<String> tags) {
}
