package dev.dankoz.BaseServer.ai.quiz.dto;

/**
 * One submitted answer. {@code answer} is type-specific:
 * String (SINGLE_CHOICE/TRUE_FALSE/OPEN), List (MULTIPLE_ANSWER/FILL_BLANK/ORDERING),
 * or Map (MATCHING).
 */
public record AnswerSubmission(String questionId, Object answer) {
}
