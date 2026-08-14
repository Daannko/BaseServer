package dev.dankoz.BaseServer.ai.quiz.dto;

import java.time.Instant;
import java.util.List;

import dev.dankoz.BaseServer.ai.quiz.model.Question;
import dev.dankoz.BaseServer.ai.quiz.model.Quiz;

/** Full quiz with questions. {@code reveal} controls answer-key exposure per question. */
public record QuizResponse(
        String id,
        String title,
        List<String> tags,
        String boardId,
        List<String> sourceNoteIds,
        Double coverage,
        Boolean explainOptions,
        int questionCount,
        Instant createdAt,
        Instant updatedAt,
        List<QuestionResponse> questions) {

    public static QuizResponse of(Quiz quiz, boolean reveal) {
        List<Question> qs = quiz.getQuestions() == null ? List.of() : quiz.getQuestions();
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTags(),
                quiz.getBoardId(),
                quiz.getSourceNoteIds(),
                quiz.getCoverage(),
                quiz.getExplainOptions(),
                quiz.questionCount(),
                quiz.getCreatedAt(),
                quiz.getUpdatedAt(),
                qs.stream().map(q -> QuestionResponse.of(q, reveal)).toList());
    }
}
