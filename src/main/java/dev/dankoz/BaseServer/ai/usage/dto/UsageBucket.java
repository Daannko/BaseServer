package dev.dankoz.BaseServer.ai.usage.dto;

/** Token usage grouped by a key (feature or model). */
public record UsageBucket(String key, long totalTokens, long requests) {
}
