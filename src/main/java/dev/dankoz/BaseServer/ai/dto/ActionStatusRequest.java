package dev.dankoz.BaseServer.ai.dto;

/**
 * Persist a single action's resolution, addressed by position in the stored conversation
 * (conversations are append-only, so indices are stable).
 *
 * @param messageIndex 0-based index into the session's messages (user + assistant)
 * @param actionIndex  0-based index into that message's actions
 * @param status       pending | applied | rejected
 */
public record ActionStatusRequest(Integer messageIndex, Integer actionIndex, String status) {
}
