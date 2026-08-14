package dev.dankoz.BaseServer.ai.quiz.dto;

import java.util.List;

/** Distinct quiz-level tags with counts. */
public record TagListResponse(List<TagCount> tags) {
}
