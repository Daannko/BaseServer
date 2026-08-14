package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

/** Paged list of quiz summaries. */
public record QuizListResponse(List<QuizSummaryResponse> quizzes, long total) {
}
