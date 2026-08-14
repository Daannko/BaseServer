package dev.dankoz.BaseServer.ai.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import dev.dankoz.BaseServer.ai.dto.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A persisted conversation message. Like {@link ChatMessage} ({@code role}/{@code content})
 * but additionally carries the assistant's proposed board {@code actions} with their status.
 * {@code actions} is only set on assistant messages (null/empty for user messages).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoredMessage {

    /** system | user | assistant. */
    private String role;

    private String content;

    private List<StoredAction> actions;

    public static StoredMessage user(String content) {
        return StoredMessage.builder().role("user").content(content).build();
    }

    public static StoredMessage assistant(String content, List<StoredAction> actions) {
        return StoredMessage.builder().role("assistant").content(content).actions(actions).build();
    }

    /** Provider-wire view (drops actions). */
    public ChatMessage toChatMessage() {
        return new ChatMessage(role, content);
    }
}
