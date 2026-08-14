package dev.dankoz.BaseServer.ai.quiz.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A single quiz question. Embedded inside {@link Quiz}. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    /** Unique within the quiz — used to submit/grade answers. */
    private String id;

    private QuestionType type;

    /** Prompt; may contain {@code ___} blanks for FILL_BLANK. */
    private String question;

    /** Optional weight; defaults to 1 when null (see {@link #weight()}). */
    private Integer points;

    /** Choice-style prompt material (SINGLE_CHOICE, MULTIPLE_ANSWER, ORDERING). */
    private List<Option> options;

    /** MATCHING left column. */
    private List<Option> left;

    /** MATCHING right column. */
    private List<Option> right;

    /** Server-side only; stripped in taking mode. */
    private AnswerKey answerKey;

    private List<String> tags;

    /** Revealed after answering. */
    private String explanation;

    /** Exact note id(s) this question derives from (subset of input). Empty for expansion material. */
    private List<String> sourceNoteIds;

    /** @deprecated kept for back-compat; {@link #sourceNoteIds} is authoritative. */
    @Deprecated
    private String sourceNoteId;

    /** Effective weight used in scoring. */
    public int weight() {
        return points == null ? 1 : points;
    }
}
