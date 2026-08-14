package dev.dankoz.BaseServer.ai.provider;

import dev.dankoz.BaseServer.ai.dto.ChatRequest;
import dev.dankoz.BaseServer.ai.dto.ChatResponse;

/**
 * A chat AI provider. Implementations are auto-discovered by {@link dev.dankoz.BaseServer.ai.AiService}
 * and keyed by {@link #name()}. Add a new impl to support another provider.
 */
public interface AiProvider {

    /** Unique provider key, e.g. {@code deepseek}. Matched against {@code ai.provider}. */
    String name();

    ChatResponse chat(ChatRequest request);
}
