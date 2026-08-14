package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import dev.dankoz.BaseServer.ai.quiz.model.AnswerKey;
import dev.dankoz.BaseServer.ai.quiz.model.Option;
import dev.dankoz.BaseServer.ai.quiz.model.Question;
import dev.dankoz.BaseServer.ai.quiz.model.QuestionType;

/**
 * Wire view of a question. In taking mode {@code answerKey} and {@code explanation}
 * are null (and omitted by {@link JsonInclude}); reveal mode includes them.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionResponse(
        String id,
        QuestionType type,
        String question,
        Integer points,
        List<Option> options,
        List<Option> left,
        List<Option> right,
        AnswerKey answerKey,
        List<String> tags,
        String explanation,
        List<String> sourceNoteIds,
        String sourceNoteId) {

    /** Map a stored question to a wire view, optionally revealing grading data. */
    public static QuestionResponse of(Question q, boolean reveal) {
        return new QuestionResponse(
                q.getId(),
                q.getType(),
                q.getQuestion(),
                q.getPoints(),
                // Per-option explanations are answer-leaking — strip them in taking mode.
                reveal ? q.getOptions() : strip(q.getOptions()),
                q.getLeft(),
                q.getRight(),
                reveal ? q.getAnswerKey() : null,
                q.getTags(),
                reveal ? q.getExplanation() : null,
                q.getSourceNoteIds(),
                q.getSourceNoteId());
    }

    private static List<Option> strip(List<Option> options) {
        if (options == null) {
            return null;
        }
        return options.stream().map(Option::stripped).toList();
    }
}
