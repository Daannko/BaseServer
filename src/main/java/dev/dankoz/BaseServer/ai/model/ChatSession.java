package dev.dankoz.BaseServer.ai.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A persisted chat conversation. Stores the full message history for replay. */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chat_sessions")
public class ChatSession {

    @Id
    private String id;

    /** Owner user id — sessions are scoped per user. */
    private Integer ownerId;

    /** Board this conversation belongs to (board chat); null for provider-only sessions. */
    private String boardId;

    /** Short server-derived label, set on the first turn. */
    private String title;

    /** Provider used for this session (e.g. {@code deepseek}). */
    private String provider;

    private List<StoredMessage> messages;

    private Instant createdAt;
    private Instant updatedAt;
}
