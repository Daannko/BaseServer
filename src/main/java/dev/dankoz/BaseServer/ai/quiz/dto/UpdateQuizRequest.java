package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

import dev.dankoz.BaseServer.ai.quiz.model.Question;

/** Partial update of a stored quiz. Omitted (null) fields are left untouched. */
public record UpdateQuizRequest(
        String title,
        List<String> tags,
        List<Question> questions) {
}
