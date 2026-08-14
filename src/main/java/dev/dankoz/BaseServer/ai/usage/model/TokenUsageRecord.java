package dev.dankoz.BaseServer.ai.usage.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row per AI request, recording token usage attributed to a user.
 * Persisted in Postgres (the user's primary DB). {@code userId} is a plain column,
 * not a relation, to keep AI accounting decoupled from the User aggregate.
 */
@Entity
@Table(name = "ai_token_usage", indexes = @Index(name = "idx_token_usage_user", columnList = "userId"))
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer userId;

    private String provider;
    private String model;

    /** What the tokens were spent on, e.g. {@code QUIZ_GENERATE}, {@code QUIZ_EVALUATE}, {@code CHAT}. */
    private String feature;

    private int promptTokens;
    private int completionTokens;
    private int totalTokens;

    private Instant createdAt;
}
