package dev.dankoz.BaseServer.ai.quiz.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A labelled choice / match item: {@code key} is the stable handle, {@code text} the display.
 *
 * <p>{@code explanation} is an optional per-option reason (why it's correct / wrong), populated
 * only for closed questions when the quiz was generated with {@code explainOptions}. Like the
 * answer key it is answer-leaking, so it is stripped in taking mode and revealed on
 * reveal/evaluate. Always {@code null} for MATCHING left/right items.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Option(String key, String text, String explanation) {

    /** Back-compat constructor for options without a per-option explanation. */
    public Option(String key, String text) {
        this(key, text, null);
    }

    /** Taking-mode view: drop the answer-leaking explanation. */
    public Option stripped() {
        return explanation == null ? this : new Option(key, text, null);
    }
}
