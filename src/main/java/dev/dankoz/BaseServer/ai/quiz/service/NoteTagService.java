package dev.dankoz.BaseServer.ai.quiz.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.dankoz.BaseServer.ai.AiService;
import dev.dankoz.BaseServer.ai.dto.ChatMessage;
import dev.dankoz.BaseServer.ai.dto.ChatRequest;
import dev.dankoz.BaseServer.ai.dto.ChatResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.NoteInput;
import dev.dankoz.BaseServer.ai.quiz.util.TiptapText;
import dev.dankoz.BaseServer.app.board.model.Board;
import dev.dankoz.BaseServer.app.board.model.NoteElement;
import dev.dankoz.BaseServer.app.board.repository.BoardRepository;
import dev.dankoz.BaseServer.app.board.repository.ElementRepository;
import dev.dankoz.BaseServer.config.properties.AiProperties;
import org.springframework.stereotype.Service;

/**
 * Extracts general topic tags from notes using a cheap model and persists them on the
 * {@link NoteElement}. Tags are cached: while a note's content hash matches the stored
 * {@code tagsHash} the saved tags are reused; a changed note (different hash, or tags wiped
 * on update) triggers a fresh extraction.
 *
 * <p>All notes that need extraction are sent to the model in a single request so it sees the
 * whole study set at once (better, more consistent topics) and tags each note for what IT
 * covers. Cached notes are skipped from the batch. Used by the quiz "extract tags" mode so the
 * generator works from general topics rather than verbatim note text.
 */
@Service
public class NoteTagService {

    private static final int MAX_TAGS_PER_NOTE = 8;

    private final AiService aiService;
    private final ElementRepository elementRepository;
    private final BoardRepository boardRepository;
    private final String tagModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NoteTagService(AiService aiService, ElementRepository elementRepository,
                          BoardRepository boardRepository, AiProperties aiProperties) {
        this.aiService = aiService;
        this.elementRepository = elementRepository;
        this.boardRepository = boardRepository;
        String configured = aiProperties.deepseek() == null ? null : aiProperties.deepseek().tagModel();
        this.tagModel = (configured == null || configured.isBlank()) ? null : configured;
    }

    /**
     * Tag extraction result.
     *
     * @param byNote     per-note tags, keyed by note id (may be empty for a note with no clear topic)
     * @param union      distinct tags across all notes, ranked by {@code tagNoteCounts} (most-shared first)
     * @param tagNoteCounts how many notes mention each tag (display form -> count); drives quiz weighting
     * @param noteCount  number of notes that produced tags (the denominator for the counts)
     */
    public record NoteTags(Map<String, List<String>> byNote, List<String> union,
                           Map<String, Integer> tagNoteCounts, int noteCount) {
    }

    /** A note queued for batch extraction (cache miss). */
    private record Pending(String id, String text, String hash, NoteElement element) {
    }

    /**
     * Resolve tags for every note: reuse persisted tags when the content is unchanged, otherwise
     * batch the cache-misses into a single model call (the model sees all of them together) and
     * persist the results per note. Tags are then aggregated with per-tag note-frequency so the
     * generator can weight recurring core topics over both one-off outliers and near-ubiquitous
     * (generic) tags.
     */
    public NoteTags extract(List<NoteInput> notes, Integer ownerId) {
        List<String> order = new ArrayList<>();          // note ids in request order
        Map<String, List<String>> resolved = new LinkedHashMap<>();
        List<Pending> pending = new ArrayList<>();

        for (NoteInput n : notes) {
            if (n == null || n.id() == null) {
                continue;
            }
            order.add(n.id());
            String text = TiptapText.toPlainText(n.content());
            String hash = hash(text);
            NoteElement element = loadNote(n.id());

            if (element != null && hash.equals(element.getTagsHash()) && element.getTags() != null) {
                resolved.put(n.id(), element.getTags()); // cache hit (empty list is a valid cached result)
            } else if (text.isBlank()) {
                persist(element, List.of(), hash);       // contentless note => no tags, cache it
                resolved.put(n.id(), List.of());
            } else {
                pending.add(new Pending(n.id(), text, hash, element));
            }
        }

        if (!pending.isEmpty()) {
            Map<Integer, List<String>> batch = extractBatch(pending, ownerId);
            for (int i = 0; i < pending.size(); i++) {
                Pending p = pending.get(i);
                List<String> tags = batch.getOrDefault(i, List.of());
                persist(p.element(), tags, p.hash());
                resolved.put(p.id(), tags);
            }
        }

        return aggregate(order, resolved);
    }

    /** Build the per-note map (request order), the frequency counts and the ranked union. */
    private NoteTags aggregate(List<String> order, Map<String, List<String>> resolved) {
        Map<String, List<String>> byNote = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>(); // keyed by lowercase tag
        Map<String, String> display = new LinkedHashMap<>();  // lowercase -> first readable form

        for (String id : order) {
            List<String> tags = resolved.getOrDefault(id, List.of());
            byNote.put(id, tags);
            for (String tag : tags) {
                String key = tag.toLowerCase();
                counts.merge(key, 1, Integer::sum);
                display.putIfAbsent(key, tag);
            }
        }

        // Rank most-shared first; build display->count and the ordered union in one pass.
        Map<String, Integer> tagNoteCounts = new LinkedHashMap<>();
        List<String> union = new ArrayList<>();
        counts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> {
                    String label = display.get(e.getKey());
                    tagNoteCounts.put(label, e.getValue());
                    union.add(label);
                });

        return new NoteTags(byNote, union, tagNoteCounts, byNote.size());
    }

    /**
     * One model call for all cache-missed notes. Returns tags keyed by the note's index in
     * {@code pending} (0-based). Missing entries default to no tags. Never throws.
     */
    private Map<Integer, List<String>> extractBatch(List<Pending> pending, Integer ownerId) {
        String subject = boardContext(pending);
        ChatRequest request = new ChatRequest(
                List.of(ChatMessage.system(systemPrompt()), ChatMessage.user(batchUserPrompt(pending, subject))),
                tagModel, 0.0, null, null, "json_object", null, null);
        Map<Integer, List<String>> out = new LinkedHashMap<>();
        try {
            ChatResponse reply = aiService.chatStatelessFor(request, ownerId, "QUIZ_EXTRACT_TAGS");
            JsonNode notesNode = objectMapper.readTree(stripFences(reply.content())).path("notes");
            if (!notesNode.isArray()) {
                return out;
            }
            for (JsonNode noteNode : notesNode) {
                int idx = noteNode.path("i").asInt(-1);
                if (idx < 0 || idx >= pending.size()) {
                    continue;
                }
                out.put(idx, parseTags(noteNode.path("tags")));
            }
            return out;
        } catch (Exception e) {
            return out; // never fail generation over tag extraction
        }
    }

    /** Parse one note's tag array: strip, case-insensitive de-dupe (keep readable casing), cap. */
    private List<String> parseTags(JsonNode tagsNode) {
        if (!tagsNode.isArray()) {
            return List.of();
        }
        List<String> tags = new ArrayList<>();
        Set<String> seen = new java.util.HashSet<>();
        for (JsonNode t : tagsNode) {
            String tag = t.asText("").strip();
            if (!tag.isBlank() && seen.add(tag.toLowerCase())) {
                tags.add(tag);
            }
            if (tags.size() >= MAX_TAGS_PER_NOTE) {
                break;
            }
        }
        return tags;
    }

    /** Numbered note bodies; indices match {@code pending} so the response can key back to them. */
    private String batchUserPrompt(List<Pending> pending, String subject) {
        StringBuilder sb = new StringBuilder();
        if (!subject.isBlank()) {
            sb.append(subject).append("\n");
        }
        sb.append("Notes (").append(pending.size()).append("). Tag each one for what IT covers.\n");
        for (int i = 0; i < pending.size(); i++) {
            sb.append("\n[i=").append(i).append("]\n").append(pending.get(i).text()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Shared-subject context from the notes' board(s): name + description (set by the user when the
     * board is created). The model is told this is the umbrella subject so it skips it and tags only
     * the distinguishing sub-topics — critical when only one note is sent (no peers to infer the
     * umbrella from). Board {@code topics} is NOT used: it holds element ids, not subject text.
     */
    private String boardContext(List<Pending> pending) {
        Set<String> boardIds = new java.util.LinkedHashSet<>();
        for (Pending p : pending) {
            if (p.element() != null && p.element().getBoardId() != null) {
                boardIds.add(p.element().getBoardId());
            }
        }
        if (boardIds.isEmpty()) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        for (String boardId : boardIds) {
            Board board = boardRepository.findById(boardId).orElse(null);
            if (board == null) {
                continue;
            }
            StringBuilder b = new StringBuilder();
            if (board.getName() != null && !board.getName().isBlank()) {
                b.append(board.getName().strip());
            }
            if (board.getDescription() != null && !board.getDescription().isBlank()) {
                b.append(b.length() == 0 ? "" : " — ").append(board.getDescription().strip());
            }
            if (b.length() > 0) {
                lines.add(b.toString());
            }
        }
        if (lines.isEmpty()) {
            return "";
        }
        return "These notes come from: " + String.join(" | ", lines)
                + "\nThat is the SHARED subject — do NOT emit it as a tag; "
                + "tag only the distinguishing sub-topic each note covers within it.";
    }

    private void persist(NoteElement element, List<String> tags, String hash) {
        if (element != null) {
            element.setTags(tags);
            element.setTagsHash(hash);
            elementRepository.save(element);
        }
    }

    private NoteElement loadNote(String id) {
        return elementRepository.findById(id)
                .filter(NoteElement.class::isInstance)
                .map(NoteElement.class::cast)
                .orElse(null);
    }

    private String systemPrompt() {
        return """
                You extract the GENERAL topics each study note covers. You receive several notes at
                once, each marked with an index like [i=0]. Use the whole set as context to stay
                consistent, but tag every note ONLY for the subjects that note itself discusses.

                Return a single JSON object and nothing else:
                {"notes": [{"i": <index>, "tags": [string]}]}.
                Include one entry per input note, using its given index.

                A tag names a TOPIC / subject area someone could study — not a specific fact, rule,
                or detail from the note. Think "what is this about?", the heading you'd file it under.

                Aim for the middle level:
                - NOT a single fact or caveat. The note saying "switch does not work on strings" is
                  the topic "switch statement" (or "control flow") — NOT "switch string limitation".
                  A quiz writer should still have to choose what to ask; the tag must not give away the
                  exact question.
                - NOT the whole field or the subject the notes already share. The user message may
                  start with a "These notes come from:" line naming the shared subject — treat that as
                  the umbrella and never emit it. If no such line is given, infer the subject common to
                  most/all of these notes (e.g. a language, course, or domain like "C++" or "physics")
                  yourself and DO NOT emit it — it is true of everything and useless for distinguishing
                  notes. Instead tag each note with the SUB-TOPICS that set it apart within that shared
                  subject ("control flow", "pointers", "templates", "move semantics"; "thermodynamics",
                  "kinematics"). If the notes genuinely share no common subject, this exclusion does
                  not apply.
                  More examples: "loops", "Dijkstra's algorithm", "TCP handshake", "French Revolution",
                  "binary search trees".

                Rules:
                - Plain natural-language noun phrases only. NO code identifiers, snake_case, or
                  SCREAMING_CASE (never emit things like "C_SWITCH_STRING_RESTRICTION").
                - Output ONLY topics the note really covers. Never pad to a number, never guess.
                - There is NO minimum. A short note may yield 1 tag; a single word or contentless note
                  yields []. Quality over quantity — at most 8 per note.
                - When two notes cover the same topic, use the SAME tag wording for both.
                - Do not invent topics that are not in the note.

                Return JSON only.
                """;
    }

    private String stripFences(String content) {
        String trimmed = content == null ? "" : content.strip();
        if (trimmed.startsWith("```")) {
            int firstBreak = trimmed.indexOf('\n');
            if (firstBreak >= 0) {
                trimmed = trimmed.substring(firstBreak + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.strip();
    }

    private String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16))
                  .append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(Objects.hashCode(text)); // fallback, still stable per-content
        }
    }
}
