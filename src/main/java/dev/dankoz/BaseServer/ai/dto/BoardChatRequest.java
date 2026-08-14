package dev.dankoz.BaseServer.ai.dto;

import java.util.List;

/**
 * Board-aware chat request. The client sends the full running thread each call plus
 * optional note context; {@code sessionId} null/omitted starts a new session.
 *
 * @param boardId      board this conversation belongs to
 * @param messages     entire conversation so far, in order
 * @param selection    selected notes sent as context (may be empty)
 * @param selectedText optional highlighted text from an element
 * @param sessionId    optional existing session to append to
 */
public record BoardChatRequest(
        String boardId,
        List<ChatMessage> messages,
        List<NoteContext> selection,
        String selectedText,
        String sessionId) {
}
