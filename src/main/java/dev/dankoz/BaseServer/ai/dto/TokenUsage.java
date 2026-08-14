package dev.dankoz.BaseServer.ai.dto;

/** Token counts for a single AI request, as reported by the provider. */
public record TokenUsage(int promptTokens, int completionTokens, int totalTokens) {

    public static final TokenUsage ZERO = new TokenUsage(0, 0, 0);
}
