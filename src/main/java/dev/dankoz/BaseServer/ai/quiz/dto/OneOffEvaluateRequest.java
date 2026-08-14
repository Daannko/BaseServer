package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

import dev.dankoz.BaseServer.ai.quiz.model.Question;

/**
 * Evaluate answers for a one-off (unstored) quiz — the client sends the full
 * questions (incl. answer data) back alongside the answers.
 */
public record OneOffEvaluateRequest(QuizPayload quiz, List<AnswerSubmission> answers) {

    public record QuizPayload(List<Question> questions) {
    }
}
