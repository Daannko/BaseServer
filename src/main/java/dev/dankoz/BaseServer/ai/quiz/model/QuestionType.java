package dev.dankoz.BaseServer.ai.quiz.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Quiz question types. Drives both the client answer payload and the grading
 * strategy. Unknown/extra values must be tolerated by clients.
 */
public enum QuestionType {
    SINGLE_CHOICE,
    MULTIPLE_ANSWER,
    TRUE_FALSE,
    OPEN,
    FILL_BLANK,
    MATCHING,
    ORDERING;

    /** Accepts legacy {@code MULTIPLE_CHOICE} as an alias for {@link #SINGLE_CHOICE}. */
    @JsonCreator
    public static QuestionType from(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.equals("MULTIPLE_CHOICE")) {
            return SINGLE_CHOICE;
        }
        return QuestionType.valueOf(normalized);
    }
}
