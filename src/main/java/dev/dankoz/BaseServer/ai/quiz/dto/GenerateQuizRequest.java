package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

import dev.dankoz.BaseServer.ai.quiz.model.QuestionType;

/**
 * Request to generate (and optionally store) a quiz.
 *
 * @param notes         source notes, each optionally carrying world geometry
 * @param coverage      0.0–1.0; 1.0 = strict (only note material), 0.0 = exploratory (related only)
 * @param boardId       optional board association
 * @param questionTypes optional whitelist of allowed types; null = backend's choice. Honored strictly.
 * @param count         optional target question count (clamped 4–20)
 * @param persist       store the quiz (default true); false = one-off, not saved
 * @param prompt         optional free-text user steer; guidance only, must not override the bounds above
 * @param avoidExisting  if true, generate questions different from quizzes already linked to these notes
 * @param explainOptions if true, populate a per-option explanation (why correct/wrong) for closed
 *                       questions (SINGLE_CHOICE, MULTIPLE_ANSWER, TRUE_FALSE); gated like answer keys
 * @param extractTopics  if true, first extract general topic tags from each note (cheap model,
 *                       persisted/cached per note) and generate the quiz from the UNION of those
 *                       tags instead of the raw note text — forces focus on general topics
 */
public record GenerateQuizRequest(
        List<NoteInput> notes,
        Double coverage,
        String boardId,
        List<QuestionType> questionTypes,
        Integer count,
        Boolean persist,
        String prompt,
        Boolean avoidExisting,
        Boolean explainOptions,
        Boolean extractTopics) {
}
