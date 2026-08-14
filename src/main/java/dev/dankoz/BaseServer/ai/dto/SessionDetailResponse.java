package dev.dankoz.BaseServer.ai.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import dev.dankoz.BaseServer.ai.model.ChatSession;
import dev.dankoz.BaseServer.ai.model.StoredAction;
import dev.dankoz.BaseServer.ai.model.StoredMessage;

/** One full conversation with its messages and per-action status. */
public record SessionDetailResponse(
        String id,
        String boardId,
        String title,
        Instant createdAt,
        Instant updatedAt,
        List<MessageView> messages) {

    /** A message in chronological order; {@code actions} only on assistant messages. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MessageView(String role, String content, List<StoredAction> actions) {

        public static MessageView of(StoredMessage m) {
            List<StoredAction> actions = (m.getActions() == null || m.getActions().isEmpty())
                    ? null : m.getActions();
            return new MessageView(m.getRole(), m.getContent(), actions);
        }
    }

    public static SessionDetailResponse of(ChatSession s) {
        List<StoredMessage> msgs = s.getMessages() == null ? List.of() : s.getMessages();
        return new SessionDetailResponse(
                s.getId(),
                s.getBoardId(),
                s.getTitle(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                msgs.stream().map(MessageView::of).toList());
    }
}
