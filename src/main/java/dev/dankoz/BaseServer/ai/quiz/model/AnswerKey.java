package dev.dankoz.BaseServer.ai.quiz.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Normalized answer key. The grader switches on {@link Question#getType()}; exactly
 * one branch is meaningful per type:
 * <ul>
 *   <li>{@code keys} — SINGLE_CHOICE | TRUE_FALSE | MULTIPLE_ANSWER | FILL_BLANK (one per blank)</li>
 *   <li>{@code pairs} — MATCHING (leftKey -&gt; rightKey)</li>
 *   <li>{@code order} — ORDERING (option keys in correct order)</li>
 *   <li>{@code rubric} — OPEN / AI-graded FILL_BLANK (what a correct answer must contain)</li>
 *   <li>{@code sampleAnswer} — OPEN reference answer, shown after grading</li>
 * </ul>
 * Stored server-side only; stripped from taking-mode responses.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerKey {
    private List<String> keys;
    private Map<String, String> pairs;
    private List<String> order;
    private String rubric;
    private String sampleAnswer;
}
