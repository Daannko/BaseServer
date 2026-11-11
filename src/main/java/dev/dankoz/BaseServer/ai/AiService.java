package dev.dankoz.BaseServer.ai;

import dev.dankoz.BaseServer.ai.dto.ChatMessage;
import dev.dankoz.BaseServer.ai.dto.ChatRequest;
import dev.dankoz.BaseServer.ai.dto.ChatResponse;
import dev.dankoz.BaseServer.ai.model.ChatSession;
import dev.dankoz.BaseServer.ai.model.StoredMessage;
import dev.dankoz.BaseServer.ai.provider.AiProvider;
import dev.dankoz.BaseServer.ai.repository.ChatSessionRepository;
import dev.dankoz.BaseServer.ai.usage.TokenUsageService;
import dev.dankoz.BaseServer.config.properties.AiProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Entry point for AI chat. Selects a provider by {@code ai.provider} (default) or
 * by an explicit name. All {@link AiProvider} beans are auto-registered, so adding
 * a provider only requires a new impl.
 *
 * <p>Sessions are optional: pass a {@code sessionId} to continue a stored
 * conversation, or omit it to start a new one (the new id is returned).
 */
@Service
public class AiService {

    private final Map<String, AiProvider> providers;
    private final String defaultProvider;
    private final ChatSessionRepository sessionRepository;
    private final TokenUsageService tokenUsageService;

    public AiService(List<AiProvider> providers, AiProperties aiProperties,
                     ChatSessionRepository sessionRepository, TokenUsageService tokenUsageService) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(AiProvider::name, Function.identity()));
        this.defaultProvider = aiProperties.provider();
        this.sessionRepository = sessionRepository;
        this.tokenUsageService = tokenUsageService;
    }

    // --- Stateless (no session) ---

    /** One-shot prompt, default provider, no session stored. */
    public ChatResponse ask(String prompt) {
        return callProvider(defaultProvider, ChatRequest.of(prompt));
    }

    /** Stateless chat — only the messages in {@code request} are sent. Not recorded. */
    public ChatResponse chatStateless(ChatRequest request) {
        return callProvider(defaultProvider, request);
    }

    /**
     * Stateless chat that records token usage against {@code userId} under {@code feature}.
     * Use from internal features (quiz, etc.) so usage is attributed to a user.
     */
    public ChatResponse chatStatelessFor(ChatRequest request, Integer userId, String feature) {
        tokenUsageService.assertWithinDailyLimit(userId);
        ChatResponse response = callProvider(defaultProvider, request);
        tokenUsageService.record(userId, response, feature);
        return response;
    }

    // --- Session-aware ---

    /** Session-aware chat with the default provider, scoped to {@code ownerId}. */
    public ChatResponse chat(ChatRequest request, Integer ownerId) {
        return chat(defaultProvider, request, ownerId);
    }

    /**
     * Session-aware chat. If {@code request.sessionId()} is null a new session is
     * created; otherwise the owner's stored history is loaded and extended. The
     * returned response always carries the session id.
     */
    public ChatResponse chat(String provider, ChatRequest request, Integer ownerId) {
        tokenUsageService.assertWithinDailyLimit(ownerId);
        ChatSession session = resolveSession(request.sessionId(), ownerId, provider);

        // Full conversation = stored history + the new incoming messages.
        List<ChatMessage> conversation = new ArrayList<>();
        session.getMessages().forEach(m -> conversation.add(m.toChatMessage()));
        conversation.addAll(request.messages());

        ChatResponse reply = callProvider(provider, request.withMessages(conversation));
        tokenUsageService.record(ownerId, reply, "CHAT");

        // Persist: history + the new messages + the assistant reply.
        List<StoredMessage> stored = new ArrayList<>(session.getMessages());
        request.messages().forEach(m -> stored.add(new StoredMessage(m.role(), m.content(), null)));
        stored.add(StoredMessage.assistant(reply.content(), null));
        session.setMessages(stored);
        session.setUpdatedAt(Instant.now());
        sessionRepository.save(session);

        return reply.withSessionId(session.getId());
    }

    /** Load a session history (owner-scoped) as provider-wire messages. */
    public List<ChatMessage> getHistory(String sessionId, Integer ownerId) {
        return sessionRepository.findByIdAndOwnerId(sessionId, ownerId)
                .map(s -> s.getMessages().stream().map(StoredMessage::toChatMessage).toList())
                .orElseThrow(() -> sessionNotFound(sessionId));
    }

    // --- internals ---

    private ChatSession resolveSession(String sessionId, Integer ownerId, String provider) {
        if (sessionId == null) {
            return ChatSession.builder()
                    .ownerId(ownerId)
                    .provider(provider)
                    .messages(new ArrayList<>())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }
        return sessionRepository.findByIdAndOwnerId(sessionId, ownerId)
                .orElseThrow(() -> sessionNotFound(sessionId));
    }

    private ChatResponse callProvider(String provider, ChatRequest request) {
        AiProvider impl = providers.get(provider);
        if (impl == null) {
            throw new IllegalArgumentException("Unknown AI provider: " + provider
                    + ". Available: " + providers.keySet());
        }
        return impl.chat(request);
    }

    private IllegalArgumentException sessionNotFound(String sessionId) {
        return new IllegalArgumentException("Chat session not found: " + sessionId);
    }
}
