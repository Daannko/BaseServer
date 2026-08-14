package dev.dankoz.BaseServer.ai.dto;

import java.time.Instant;
import java.util.List;

import dev.dankoz.BaseServer.ai.model.ChatSession;

/** Lightweight list of board conversations (no messages). */
public record SessionListResponse(List<SessionSummary> sessions, long total) {

    /** One row in the session list. */
    public record SessionSummary(
            String id,
            String boardId,
            String title,
            int messageCount,
            Instant createdAt,
            Instant updatedAt) {

        public static SessionSummary of(ChatSession s) {
            return new SessionSummary(
                    s.getId(),
                    s.getBoardId(),
                    s.getTitle(),
                    s.getMessages() == null ? 0 : s.getMessages().size(),
                    s.getCreatedAt(),
                    s.getUpdatedAt());
        }
    }
}
