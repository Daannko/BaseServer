package dev.dankoz.BaseServer.ai.quiz.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A persisted quiz, owned by a user and optionally tied to a board.
 *
 * <p>{@code ownerId} is server-side scoping (not in the frontend contract) — every
 * read/write is filtered by it so users only touch their own quizzes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quizzes")
public class Quiz {

    @Id
    private String id;

    /** Owning user id — server-side scope, never exposed to clients. */
    private Integer ownerId;

    private String title;

    /** Quiz-level topic tags (union of question tags + note content). */
    private List<String> tags;

    private String boardId;

    /** Note element ids this quiz was generated from (many-to-many, queryable both ways). */
    private List<String> sourceNoteIds;

    /** 0.0–1.0 coverage used at generation time. */
    private Double coverage;

    /** Whether closed-question options carry per-option explanations (gated like answer keys). */
    private Boolean explainOptions;

    private List<Question> questions;

    private Instant createdAt;
    private Instant updatedAt;

    public int questionCount() {
        return questions == null ? 0 : questions.size();
    }
}
