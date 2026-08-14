package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

/**
 * Evaluation result.
 *
 * @param score      "sumOfWeightedScore/totalPoints", e.g. "7.5/10"
 * @param percentage 0–100 rounded
 */
public record EvaluateResponse(List<QuestionResult> results, String score, int percentage) {
}
