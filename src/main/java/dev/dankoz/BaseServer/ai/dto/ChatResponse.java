package dev.dankoz.BaseServer.ai.dto;

/**
 * Provider-agnostic chat response.
 *
 * @param content   the assistant reply text
 * @param model     the model that produced the reply
 * @param provider  the provider name that served the request
 * @param sessionId the session this exchange belongs to (null for stateless calls)
 * @param usage     token usage reported by the provider (null if unknown)
 */
public record ChatResponse(String content, String model, String provider, String sessionId, TokenUsage usage) {

    /** Stateless reply with no session attached. */
    public ChatResponse(String content, String model, String provider, TokenUsage usage) {
        this(content, model, provider, null, usage);
    }

    public ChatResponse withSessionId(String sessionId) {
        return new ChatResponse(content, model, provider, sessionId, usage);
    }
}
