package dev.dankoz.BaseServer.ai.dto;

import java.util.List;
import java.util.Map;

/**
 * Provider-agnostic chat request. All tuning fields are optional ({@code null} =
 * use provider default). {@code responseFormat} accepts {@code "text"} or
 * {@code "json_object"}. {@code extra} is a passthrough map merged into the raw
 * provider body for params not yet modeled here (forward-compat).
 *
 * <p>{@code sessionId}: when null a new session is created and its id returned in
 * the response; when set, the stored history is loaded and these messages appended.
 */
public record ChatRequest(
        List<ChatMessage> messages,
        String model,
        Double temperature,
        Double topP,
        Integer maxTokens,
        String responseFormat,
        Map<String, Object> extra,
        String sessionId) {

    public ChatRequest(List<ChatMessage> messages) {
        this(messages, null, null, null, null, null, null, null);
    }

    public ChatRequest(List<ChatMessage> messages, String model) {
        this(messages, model, null, null, null, null, null, null);
    }

    public static ChatRequest of(String prompt) {
        return new ChatRequest(List.of(ChatMessage.user(prompt)));
    }

    /** Fluent copy helpers — records are immutable. */
    public ChatRequest withMessages(List<ChatMessage> messages) {
        return new ChatRequest(messages, model, temperature, topP, maxTokens, responseFormat, extra, sessionId);
    }

    public ChatRequest withTemperature(Double temperature) {
        return new ChatRequest(messages, model, temperature, topP, maxTokens, responseFormat, extra, sessionId);
    }

    public ChatRequest withModel(String model) {
        return new ChatRequest(messages, model, temperature, topP, maxTokens, responseFormat, extra, sessionId);
    }

    public ChatRequest withResponseFormat(String responseFormat) {
        return new ChatRequest(messages, model, temperature, topP, maxTokens, responseFormat, extra, sessionId);
    }

    public ChatRequest withSessionId(String sessionId) {
        return new ChatRequest(messages, model, temperature, topP, maxTokens, responseFormat, extra, sessionId);
    }
}
