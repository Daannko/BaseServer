package dev.dankoz.BaseServer.ai.quiz.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.dankoz.BaseServer.ai.AiService;
import dev.dankoz.BaseServer.ai.dto.ChatMessage;
import dev.dankoz.BaseServer.ai.dto.ChatRequest;
import dev.dankoz.BaseServer.ai.dto.ChatResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.GenerateQuizRequest;
import dev.dankoz.BaseServer.ai.quiz.dto.NoteInput;
import dev.dankoz.BaseServer.ai.quiz.model.AnswerKey;
import dev.dankoz.BaseServer.ai.quiz.model.Option;
import dev.dankoz.BaseServer.ai.quiz.model.Question;
import dev.dankoz.BaseServer.ai.quiz.model.QuestionType;
import dev.dankoz.BaseServer.ai.quiz.model.Quiz;
import dev.dankoz.BaseServer.ai.quiz.util.TiptapText;
import dev.dankoz.BaseServer.config.exceptions.BadRequestException;
import org.springframework.stereotype.Service;

/**
 * Builds the LLM prompt, calls the AI engine, and parses the JSON reply into a
 * {@link Quiz} (not yet persisted). All prompt/judge logic lives here so the
 * frontend only sends note content + parameters.
 */
@Service
public class QuizGenerationService {

    private static final int MIN_QUESTIONS = 4;
    private static final int MAX_QUESTIONS = 20;
    private static final double DEFAULT_COVERAGE = 0.75;
    /** Cap on existing questions injected for {@code avoidExisting} to keep the prompt bounded. */
    private static final int MAX_AVOID = 40;

    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizGenerationService(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * @param avoidQuestions prompts of existing questions to steer away from (for
     *                       {@code avoidExisting}); empty = no constraint.
     */
    public Quiz generate(GenerateQuizRequest request, Integer ownerId, List<String> avoidQuestions) {
        return generate(request, ownerId, avoidQuestions, null);
    }

    /**
     * @param tagMode when non-null, generation runs in "extract tags" mode: the quiz is built from
     *                these general topic tags (note bodies are NOT sent to the model) and is linked to
     *                all selected notes. Per-tag note-frequency steers question weighting. {@code null}
     *                = normal note-text generation.
     */
    public Quiz generate(GenerateQuizRequest request, Integer ownerId, List<String> avoidQuestions,
                         NoteTagService.NoteTags tagMode) {
        List<NoteInput> notes = request.notes() == null ? List.of() : request.notes();
        if (notes.isEmpty()) {
            throw new BadRequestException("At least one note is required");
        }

        boolean tagged = tagMode != null;
        if (tagged && tagMode.union().isEmpty()) {
            throw new BadRequestException("Could not extract any tags from the selected notes");
        }

        double coverage = clampCoverage(request.coverage());
        Integer count = clampCount(request.count());
        List<QuestionType> types = request.questionTypes();
        boolean explainOptions = Boolean.TRUE.equals(request.explainOptions());

        String userPrompt = tagged
                ? tagUserPrompt(tagMode, types, count, request.prompt(), avoidQuestions, explainOptions)
                : userPrompt(notes, coverage, types, count, request.prompt(), avoidQuestions, explainOptions);

        ChatResponse reply = aiService.chatStatelessFor(new ChatRequest(
                List.of(
                        ChatMessage.system(systemPrompt()),
                        ChatMessage.user(userPrompt)),
                null, 0.5, null, null, "json_object", null, null),
                ownerId, tagged ? "QUIZ_GENERATE_TAGS" : "QUIZ_GENERATE");

        GeneratedQuiz parsed = parse(reply.content());

        List<String> noteIds = notes.stream().map(NoteInput::id).filter(Objects::nonNull).toList();
        List<Question> questions = normalizeQuestions(parsed.questions(), noteIds, types, explainOptions);
        if (questions.isEmpty()) {
            throw new BadRequestException("AI returned no usable questions");
        }
        shuffleQuiz(questions);

        // Tag mode: questions are general (built from topics, not specific notes), so leave per-question
        // sourceNoteIds empty — no "jump to note" link. Quiz-level linkage (below) is kept for filtering.

        // Tag mode: the model never sees note ids, so link the whole selection. Otherwise link ONLY
        // the notes actually used by questions (union).
        List<String> usedNoteIds = tagged ? noteIds : questions.stream()
                .flatMap(q -> q.getSourceNoteIds().stream())
                .distinct()
                .toList();
        // Tag mode: the union of note tags IS the canonical tag set for the quiz.
        List<String> quizTags = tagged ? tagMode.union()
                : (parsed.tags() != null ? parsed.tags() : List.of());

        Instant now = Instant.now();
        return Quiz.builder()
                .ownerId(ownerId)
                .title(parsed.title() != null ? parsed.title() : "Untitled quiz")
                .tags(quizTags)
                .boardId(request.boardId())
                .sourceNoteIds(usedNoteIds)
                .coverage(coverage)
                .explainOptions(explainOptions)
                .questions(questions)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // --- backfill per-option explanations on an existing question ---

    /**
     * Populate per-option explanations on a single (already generated) closed question, mutating it
     * in place. No-op for non-closed types. For TRUE_FALSE, synthetic TRUE/FALSE options are created
     * first if absent. Uses the stored {@link AnswerKey} so the model knows which option(s) are
     * correct. On AI/parse failure the question is left unchanged.
     */
    public Question explainOptions(Question q, Integer ownerId) {
        if (q == null || !isClosed(q.getType())) {
            return q;
        }
        if (q.getType() == QuestionType.TRUE_FALSE
                && (q.getOptions() == null || q.getOptions().isEmpty())) {
            q.setOptions(List.of(new Option("TRUE", "True", null), new Option("FALSE", "False", null)));
        }
        List<Option> options = q.getOptions();
        if (options == null || options.isEmpty()) {
            return q;
        }

        List<String> correctKeys = q.getAnswerKey() == null ? List.of()
                : (q.getAnswerKey().getKeys() == null ? List.of() : q.getAnswerKey().getKeys());

        try {
            ChatResponse reply = aiService.chatStatelessFor(new ChatRequest(
                    List.of(
                            ChatMessage.system(explainSystemPrompt()),
                            ChatMessage.user(explainUserPrompt(q, options, correctKeys))),
                    null, 0.3, null, null, "json_object", null, null),
                    ownerId, "QUIZ_EXPLAIN_OPTIONS");
            JsonNode map = objectMapper.readTree(stripFences(reply.content())).path("explanations");

            List<Option> filled = new ArrayList<>(options.size());
            for (Option o : options) {
                String text = map.path(o.key()).asText(null);
                filled.add((text == null || text.isBlank())
                        ? o : new Option(o.key(), o.text(), text));
            }
            q.setOptions(filled);
        } catch (Exception e) {
            // Leave the question as-is on failure rather than aborting a batch backfill.
        }
        return q;
    }

    private boolean isClosed(QuestionType type) {
        return type == QuestionType.SINGLE_CHOICE
                || type == QuestionType.MULTIPLE_ANSWER
                || type == QuestionType.TRUE_FALSE;
    }

    private String explainSystemPrompt() {
        return """
                You explain quiz answer options. Return a single JSON object and nothing else:
                {"explanations": {"<optionKey>": string, ...}}.
                Give one concise explanation (~1 sentence) per option key: for a correct option say
                why it is correct, for a wrong option why it is wrong. You may use a markdown subset
                (inline `code`, fenced ```code blocks```, **bold**, *italic*, newlines) inside the JSON
                string values. When the option is about code/programming and a short snippet would
                clarify it, include a minimal illustrative example in a fenced ```lang ... ``` block
                (a few correct lines); never add code for non-coding options. Return JSON only.
                """;
    }

    private String explainUserPrompt(Question q, List<Option> options, List<String> correctKeys) {
        StringBuilder sb = new StringBuilder();
        sb.append("Question: ").append(q.getQuestion()).append("\n");
        sb.append("Correct option key(s): ").append(String.join(", ", correctKeys)).append("\n");
        sb.append("Options:\n");
        for (Option o : options) {
            sb.append("- ").append(o.key()).append(": ").append(o.text()).append("\n");
        }
        sb.append("Return json {\"explanations\": {key: reason}} covering every option key above.");
        return sb.toString();
    }

    // --- prompt ---

    private String systemPrompt() {
        return """
                You are a quiz generator. Produce a quiz as a single JSON object and nothing else
                (no markdown fences, no commentary). The JSON must match this schema exactly:

                {
                  "title": string,
                  "tags": [string],                      // 1-8 canonical UPPERCASE topic codes for the whole quiz
                  "questions": [
                    {
                      "type": "SINGLE_CHOICE" | "MULTIPLE_ANSWER" | "TRUE_FALSE" | "OPEN" | "FILL_BLANK" | "MATCHING" | "ORDERING",
                      "question": string,               // use ___ for FILL_BLANK blanks
                      "points": number,                 // weight, default 1
                      "options": [{"key": "A", "text": string, "explanation": string}],   // SINGLE_CHOICE, MULTIPLE_ANSWER, ORDERING; explanation only when per-option explanations requested
                      "left":  [{"key": "L1", "text": string}],     // MATCHING only
                      "right": [{"key": "R1", "text": string}],     // MATCHING only
                      "answerKey": {
                        "keys": [string],               // SINGLE_CHOICE/TRUE_FALSE (1), MULTIPLE_ANSWER, FILL_BLANK (one per blank)
                        "pairs": {"L1": "R2"},          // MATCHING
                        "order": ["B","A","C"],         // ORDERING
                        "rubric": string,               // OPEN / AI-graded FILL_BLANK
                        "sampleAnswer": string          // OPEN
                      },
                      "tags": [string],                 // 1-8 canonical UPPERCASE topic codes for this question
                      "explanation": string,            // shown after answering
                      "sourceNoteIds": [string]         // exact note id(s) this question derives from; [] for expansion material
                    }
                  ]
                }

                Rules: TRUE_FALSE keys are "TRUE" or "FALSE". Only set the answerKey branch relevant to the type.
                Keep options keys as A,B,C,... Make exactly one correct option for SINGLE_CHOICE.

                Formatting: all human-readable text (question, option text, explanation, sampleAnswer)
                may use a markdown subset: inline code with backticks (`std::vector`), fenced code
                blocks (```lang ... ```), **bold**, *italic*, and newlines. Wrap code, identifiers and
                keywords in inline code, and multi-line snippets in fenced blocks. Plain text is fine
                where no formatting helps. Keep all markdown INSIDE JSON string values (escape newlines).

                Code in explanations: when an `explanation` (question-level or per-option) concerns
                code or programming and a short snippet would make it clearer, INCLUDE a minimal
                illustrative example in a fenced ```lang ... ``` block — a few correct, on-point lines.
                Only add code when it genuinely aids understanding; never add code for non-coding topics.

                Return valid JSON only.
                """;
    }

    private String userPrompt(List<NoteInput> notes, double coverage, List<QuestionType> types,
                              Integer count, String userSteer, List<String> avoidQuestions,
                              boolean explainOptions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a quiz from the following notes.\n\n");

        for (NoteInput note : notes) {
            String text = TiptapText.toPlainText(note.content());
            sb.append("--- NOTE id=").append(note.id()).append(" ---\n")
              .append(text.isBlank() ? "(empty)" : text).append("\n\n");
        }

        String layout = layoutSummary(notes);
        if (!layout.isEmpty()) {
            sb.append(layout).append("\n");
        }

        sb.append("Coverage = ").append(coverage).append(". ")
          .append(coverageInstruction(coverage)).append("\n");

        if (types != null && !types.isEmpty()) {
            sb.append("Use ONLY these question types (any other type is forbidden): ")
              .append(types.stream().map(Enum::name).collect(Collectors.joining(", ")))
              .append(".\n");
        }
        if (count != null) {
            sb.append("Produce exactly ").append(count).append(" questions.\n");
        } else {
            sb.append("Choose a sensible number of questions (")
              .append(MIN_QUESTIONS).append("-").append(MAX_QUESTIONS).append(").\n");
        }

        sb.append("Set sourceNoteIds to the array of note id(s) each question is based on "
                + "(a subset of the ids above), or [] for new/expansion material.\n");

        if (explainOptions) {
            sb.append("\nFor every SINGLE_CHOICE, MULTIPLE_ANSWER and TRUE_FALSE question, set an "
                    + "`explanation` on EACH option: for a correct option explain briefly why it is "
                    + "correct, for a wrong option why it is wrong (concise, ~1 sentence).\n");
            sb.append("For TRUE_FALSE include an options array exactly like "
                    + "[{\"key\":\"TRUE\",\"text\":\"True\",\"explanation\":...},"
                    + "{\"key\":\"FALSE\",\"text\":\"False\",\"explanation\":...}].\n");
        }

        if (avoidQuestions != null && !avoidQuestions.isEmpty()) {
            sb.append("\nThe following questions ALREADY EXIST for these notes. Produce DIFFERENT "
                    + "questions covering new angles or sub-topics; do not rephrase or duplicate them:\n");
            avoidQuestions.stream().limit(MAX_AVOID)
                    .forEach(q -> sb.append("- ").append(q).append("\n"));
        }

        if (userSteer != null && !userSteer.isBlank()) {
            // Guidance only — must not override coverage/types/count bounds above.
            sb.append("\nAdditional user guidance (steer only, keep the constraints above): ")
              .append(userSteer.strip()).append("\n");
        }

        sb.append("\nReturn the quiz as a single json object per the schema.\n");
        return sb.toString();
    }

    /**
     * "Extract tags" mode prompt: the model is given ONLY the general topic tags (no note bodies)
     * and must build a quiz about those topics. This deliberately keeps the quiz related to the
     * notes (the tags came from them) while preventing it from quoting or testing verbatim note text.
     *
     * <p>Each tag carries how many notes mention it. The model is told to weight question volume by
     * that frequency, but to ease off BOTH extremes: a tag in nearly every note is generic/
     * non-discriminating, and a tag in just one (of many) notes is peripheral/possibly incidental —
     * the recurring middle is the real core.
     */
    private String tagUserPrompt(NoteTagService.NoteTags tagData, List<QuestionType> types,
                                 Integer count, String userSteer, List<String> avoidQuestions,
                                 boolean explainOptions) {
        int n = tagData.noteCount();
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a quiz about the following topics, extracted from ").append(n)
          .append(n == 1 ? " note." : " notes.\n");
        sb.append("Each topic shows how many of those notes mention it (mentions/").append(n).append("):\n");
        for (String tag : tagData.union()) {
            sb.append("- ").append(tag)
              .append(" (").append(tagData.tagNoteCounts().getOrDefault(tag, 1)).append("/").append(n).append(")\n");
        }

        sb.append("\nThese topics were extracted from a learner's study notes. Write questions that "
                + "test understanding of these GENERAL topics. Do NOT quote, paraphrase, or assume any "
                + "specific note text — you have not been shown the notes, only their topics.\n");

        if (n > 1) {
            sb.append("\nWeight coverage by importance, not evenly:\n")
              .append("- Focus most questions on RECURRING core topics (mentioned across several notes).\n")
              .append("- Give FEWER questions to topics mentioned in only one note — they are peripheral "
                      + "and may be incidental or off-theme.\n")
              .append("- Also ease off topics mentioned in (nearly) ALL notes — they are background/"
                      + "generic and poor at discriminating understanding.\n")
              .append("- A topic's importance is highest when it recurs in a meaningful share of notes "
                      + "without being in essentially all of them.\n");
        } else {
            sb.append("\nSpread questions across the listed topics.\n");
        }
        sb.append("Leave sourceNoteIds as [] for every question (no note text was provided).\n");

        if (types != null && !types.isEmpty()) {
            sb.append("Use ONLY these question types (any other type is forbidden): ")
              .append(types.stream().map(Enum::name).collect(Collectors.joining(", ")))
              .append(".\n");
        }
        if (count != null) {
            sb.append("Produce exactly ").append(count).append(" questions.\n");
        } else {
            sb.append("Choose a sensible number of questions (")
              .append(MIN_QUESTIONS).append("-").append(MAX_QUESTIONS).append(").\n");
        }

        if (explainOptions) {
            sb.append("\nFor every SINGLE_CHOICE, MULTIPLE_ANSWER and TRUE_FALSE question, set an "
                    + "`explanation` on EACH option: for a correct option explain briefly why it is "
                    + "correct, for a wrong option why it is wrong (concise, ~1 sentence).\n");
            sb.append("For TRUE_FALSE include an options array exactly like "
                    + "[{\"key\":\"TRUE\",\"text\":\"True\",\"explanation\":...},"
                    + "{\"key\":\"FALSE\",\"text\":\"False\",\"explanation\":...}].\n");
        }

        if (avoidQuestions != null && !avoidQuestions.isEmpty()) {
            sb.append("\nThe following questions ALREADY EXIST for these topics. Produce DIFFERENT "
                    + "questions covering new angles or sub-topics; do not rephrase or duplicate them:\n");
            avoidQuestions.stream().limit(MAX_AVOID)
                    .forEach(q -> sb.append("- ").append(q).append("\n"));
        }

        if (userSteer != null && !userSteer.isBlank()) {
            sb.append("\nAdditional user guidance (steer only, keep the constraints above): ")
              .append(userSteer.strip()).append("\n");
        }

        sb.append("\nReturn the quiz as a single json object per the schema.\n");
        return sb.toString();
    }

    private String layoutSummary(List<NoteInput> notes) {
        if (notes.stream().noneMatch(NoteInput::hasGeometry)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(
                "Note layout in world coordinates (use to understand grouping and relative position):\n");
        for (NoteInput n : notes) {
            if (n.hasGeometry()) {
                sb.append("- ").append(n.id())
                  .append(": pos(").append(num(n.x())).append(",").append(num(n.y())).append(")");
                if (n.width() != null && n.height() != null) {
                    sb.append(" size ").append(num(n.width())).append("x").append(num(n.height()));
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String num(Double d) {
        if (d == null) {
            return "?";
        }
        return d == Math.floor(d) ? String.valueOf((long) (double) d) : String.valueOf(d);
    }

    private String coverageInstruction(double coverage) {
        if (coverage >= 0.85) {
            return "Strict: only test topics and claims already present in the notes; introduce nothing new.";
        }
        if (coverage <= 0.15) {
            return "Exploratory: only test topics RELATED to the notes; never test anything already stated in them.";
        }
        return "Mixed: base most questions on the notes but introduce some related side topics not explicitly stated. "
                + "Roughly " + Math.round(coverage * 100) + "% known-material vs the rest expansion-material.";
    }

    // --- parsing / normalization ---

    private GeneratedQuiz parse(String content) {
        try {
            return objectMapper.readValue(stripFences(content), GeneratedQuiz.class);
        } catch (Exception e) {
            throw new BadRequestException("AI returned malformed quiz JSON: " + e.getMessage());
        }
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

    /**
     * Ensure unique ids and default points, enforce the {@code questionTypes} whitelist
     * (drop out-of-set questions), and resolve {@code sourceNoteIds} to ids the client
     * actually sent (with singular {@code sourceNoteId} back-compat both ways).
     */
    private List<Question> normalizeQuestions(List<Question> questions, List<String> noteIds,
                                              List<QuestionType> allowedTypes, boolean explainOptions) {
        if (questions == null) {
            return List.of();
        }
        Set<QuestionType> allowed = (allowedTypes == null || allowedTypes.isEmpty())
                ? null : new HashSet<>(allowedTypes);

        List<Question> out = new ArrayList<>();
        for (Question q : questions) {
            if (q == null || q.getType() == null || q.getQuestion() == null) {
                continue;
            }
            // Hard filter: the frontend only renders the requested types.
            if (allowed != null && !allowed.contains(q.getType())) {
                continue;
            }
            q.setId(UUID.randomUUID().toString());
            if (q.getPoints() == null) {
                q.setPoints(1);
            }
            normalizeOptions(q, explainOptions);

            List<String> src = q.getSourceNoteIds();
            if ((src == null || src.isEmpty()) && q.getSourceNoteId() != null) {
                src = List.of(q.getSourceNoteId());
            }
            List<String> filtered = src == null ? List.of()
                    : src.stream().filter(noteIds::contains).distinct().toList();
            q.setSourceNoteIds(filtered);
            q.setSourceNoteId(filtered.isEmpty() ? null : filtered.get(0));

            out.add(q);
        }
        return out;
    }

    /**
     * Per-option explanations live only on closed questions and only when requested. Strip them
     * everywhere else (incl. ORDERING/MATCHING items) so they neither persist nor leak. When
     * requested, backfill synthetic TRUE/FALSE options if the model omitted them.
     */
    private void normalizeOptions(Question q, boolean explainOptions) {
        boolean closed = q.getType() == QuestionType.SINGLE_CHOICE
                || q.getType() == QuestionType.MULTIPLE_ANSWER
                || q.getType() == QuestionType.TRUE_FALSE;

        if (!explainOptions || !closed) {
            q.setOptions(stripExplanations(q.getOptions()));
            return;
        }

        if (q.getType() == QuestionType.TRUE_FALSE
                && (q.getOptions() == null || q.getOptions().isEmpty())) {
            q.setOptions(List.of(new Option("TRUE", "True", null), new Option("FALSE", "False", null)));
        }
        // closed + explainOptions: keep option explanations as produced.
    }

    // --- shuffling ---

    /**
     * Randomize question order and, per question, the order of its answer options, so model bias
     * (correct option listed first, questions following note order) never shows through. Runs once
     * at generation time; the shuffled order is what gets persisted.
     */
    private void shuffleQuiz(List<Question> questions) {
        Collections.shuffle(questions);
        for (Question q : questions) {
            shuffleOptions(q);
        }
    }

    /**
     * Shuffle the answer options of one question. Choice-style options (SINGLE_CHOICE,
     * MULTIPLE_ANSWER, ORDERING) are re-lettered in the new display order — reusing the original
     * key sequence — and the answer key's {@code keys}/{@code order} references are remapped so
     * grading is unaffected. MATCHING shuffles both columns; {@code pairs} is key-based and stays
     * valid. TRUE_FALSE keeps the conventional True/False order; other types have no options.
     */
    private void shuffleOptions(Question q) {
        if (q.getType() == QuestionType.MATCHING) {
            q.setLeft(shuffled(q.getLeft()));
            q.setRight(shuffled(q.getRight()));
            return;
        }
        boolean keyed = q.getType() == QuestionType.SINGLE_CHOICE
                || q.getType() == QuestionType.MULTIPLE_ANSWER
                || q.getType() == QuestionType.ORDERING;
        List<Option> options = q.getOptions();
        if (!keyed || options == null || options.size() < 2) {
            return;
        }

        List<Option> mixed = new ArrayList<>(options);
        Collections.shuffle(mixed);

        // Duplicate/null keys would make remapping ambiguous — keep the original keys attached to
        // their options in that case; the answer key still references them correctly.
        long distinctKeys = options.stream().map(Option::key).filter(Objects::nonNull).distinct().count();
        if (distinctKeys != options.size()) {
            q.setOptions(mixed);
            return;
        }

        Map<String, String> remap = new HashMap<>();
        List<Option> rekeyed = new ArrayList<>(mixed.size());
        for (int i = 0; i < mixed.size(); i++) {
            String newKey = options.get(i).key();
            Option o = mixed.get(i);
            remap.put(o.key(), newKey);
            rekeyed.add(new Option(newKey, o.text(), o.explanation()));
        }
        q.setOptions(rekeyed);

        AnswerKey answerKey = q.getAnswerKey();
        if (answerKey != null) {
            answerKey.setKeys(remapKeys(answerKey.getKeys(), remap));
            answerKey.setOrder(remapKeys(answerKey.getOrder(), remap));
        }
    }

    private List<String> remapKeys(List<String> keys, Map<String, String> remap) {
        if (keys == null) {
            return null;
        }
        return keys.stream().map(k -> remap.getOrDefault(k, k)).toList();
    }

    private <T> List<T> shuffled(List<T> items) {
        if (items == null || items.size() < 2) {
            return items;
        }
        List<T> copy = new ArrayList<>(items);
        Collections.shuffle(copy);
        return copy;
    }

    private List<Option> stripExplanations(List<Option> options) {
        if (options == null) {
            return null;
        }
        return options.stream().map(Option::stripped).toList();
    }

    private double clampCoverage(Double coverage) {
        if (coverage == null) {
            return DEFAULT_COVERAGE;
        }
        return Math.max(0.0, Math.min(1.0, coverage));
    }

    private Integer clampCount(Integer count) {
        if (count == null) {
            return null;
        }
        return Math.max(MIN_QUESTIONS, Math.min(MAX_QUESTIONS, count));
    }

    /** Transient shape the AI fills; mapped into a {@link Quiz}. */
    private record GeneratedQuiz(String title, List<String> tags, List<Question> questions) {
    }
}
