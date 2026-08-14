package dev.dankoz.BaseServer.ai.quiz.dto;

/** A distinct quiz-level tag and how many of the user's quizzes carry it. */
public record TagCount(String tag, long count) {
}
