package dev.dankoz.BaseServer.ai.quiz.dto;

import java.time.Instant;
import java.util.List;

import dev.dankoz.BaseServer.ai.quiz.model.Quiz;

/** Quiz summary for list endpoints — no {@code questions} array. */
public record QuizSummaryResponse(
        String id,
        String title,
        List<String> tags,
        String boardId,
        int questionCount,
        Double coverage,
        Instant createdAt,
        Instant updatedAt) {

    public static QuizSummaryResponse of(Quiz quiz) {
        return new QuizSummaryResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTags(),
                quiz.getBoardId(),
                quiz.questionCount(),
                quiz.getCoverage(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt());
    }
}
