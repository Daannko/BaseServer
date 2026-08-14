package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

/** Evaluate answers against a stored quiz (server already holds the questions + keys). */
public record EvaluateRequest(List<AnswerSubmission> answers) {
}
