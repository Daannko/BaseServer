package dev.dankoz.BaseServer.ai.dto;

import java.util.List;

import dev.dankoz.BaseServer.ai.model.BoardAction;

/**
 * Board chat reply: assistant message, proposed board actions, and the session it was
 * stored in. {@code title} is the server-derived session label (present on the first turn).
 */
public record BoardChatResponse(
        String message,
        List<BoardAction> actions,
        String sessionId,
        String title) {
}
