package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import dev.dankoz.BaseServer.ai.quiz.model.Option;

/**
 * Grading result for one question.
 *
 * @param correct       boolean verdict (threshold-based for OPEN)
 * @param score         0.0–1.0 partial credit
 * @param correctAnswer canonical right answer, type-specific, for display
 * @param feedback      AI note for OPEN/FILL_BLANK (why this score); null otherwise
 * @param explanation   the question's stored explanation
 * @param options       closed-question options incl. per-option explanations (post-answer reveal); null otherwise
 * @param sourceNoteIds note id(s) this question derives from — drives the results-view "jump to note" link
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionResult(
        String questionId,
        boolean correct,
        double score,
        Object correctAnswer,
        String feedback,
        String explanation,
        List<Option> options,
        List<String> sourceNoteIds) {
}
