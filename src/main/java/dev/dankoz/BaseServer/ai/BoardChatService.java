package dev.dankoz.BaseServer.ai;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.dankoz.BaseServer.ai.dto.ActionStatusRequest;
import dev.dankoz.BaseServer.ai.dto.BoardChatRequest;
import dev.dankoz.BaseServer.ai.dto.BoardChatResponse;
import dev.dankoz.BaseServer.ai.dto.ChatMessage;
import dev.dankoz.BaseServer.ai.dto.ChatRequest;
import dev.dankoz.BaseServer.ai.dto.ChatResponse;
import dev.dankoz.BaseServer.ai.dto.NoteContext;
import dev.dankoz.BaseServer.ai.dto.SessionDetailResponse;
import dev.dankoz.BaseServer.ai.dto.SessionListResponse;
import dev.dankoz.BaseServer.ai.dto.SessionListResponse.SessionSummary;
import dev.dankoz.BaseServer.ai.model.BoardAction;
import dev.dankoz.BaseServer.ai.model.ChatSession;
import dev.dankoz.BaseServer.ai.model.StoredAction;
import dev.dankoz.BaseServer.ai.model.StoredMessage;
import dev.dankoz.BaseServer.ai.quiz.util.TiptapText;
import dev.dankoz.BaseServer.ai.repository.ChatSessionRepository;
import dev.dankoz.BaseServer.config.exceptions.BadRequestException;
import dev.dankoz.BaseServer.config.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

/**
 * Board-aware chat: drives the LLM to produce a reply plus proposed board actions, persists
 * each turn into a per-board {@link ChatSession}, and serves the conversation history/status.
 *
 * <p>The client is the source of truth for the live thread (it sends the full {@code messages}
 * each call); the server stores history for replay and appends only the new user turn + reply.
 */
@Service
public class BoardChatService {

    private static final int TITLE_MAX = 60;

    private final AiService aiService;
    private final ChatSessionRepository sessionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BoardChatService(AiService aiService, ChatSessionRepository sessionRepository) {
        this.aiService = aiService;
        this.sessionRepository = sessionRepository;
    }

    // --- chat ---

    public BoardChatResponse chat(BoardChatRequest request, Integer ownerId) {
        List<ChatMessage> incoming = request.messages() == null ? List.of() : request.messages();
        if (incoming.isEmpty()) {
            throw new BadRequestException("messages is required");
        }
        ChatSession session = resolveSession(request.sessionId(), request.boardId(), ownerId);
        boolean firstTurn = session.getMessages().isEmpty();

        // Provider conversation: system + note context + the client's full thread.
        List<ChatMessage> conversation = new ArrayList<>();
        conversation.add(ChatMessage.system(systemPrompt()));
        String context = buildContext(request.selection(), request.selectedText());
        if (!context.isBlank()) {
            conversation.add(ChatMessage.system(context));
        }
        conversation.addAll(incoming);

        ChatResponse reply = aiService.chatStatelessFor(new ChatRequest(
                conversation, null, 0.5, null, null, "json_object", null, null),
                ownerId, "CHAT");
        Parsed parsed = parse(reply.content());

        // Title from the first user message on a new session.
        if (firstTurn && session.getTitle() == null) {
            session.setTitle(deriveTitle(incoming));
        }

        // Persist the new user turn (trailing user message) + assistant reply with actions.
        List<StoredMessage> stored = new ArrayList<>(session.getMessages());
        ChatMessage lastUser = lastUserMessage(incoming);
        if (lastUser != null) {
            stored.add(StoredMessage.user(lastUser.content()));
        }
        stored.add(StoredMessage.assistant(parsed.message(), toStoredActions(parsed.actions())));
        session.setMessages(stored);
        session.setUpdatedAt(Instant.now());
        session = sessionRepository.save(session);

        return new BoardChatResponse(parsed.message(), parsed.actions(),
                session.getId(), session.getTitle());
    }

    // --- sessions ---

    public SessionListResponse listSessions(String boardId, Integer ownerId) {
        List<SessionSummary> sessions = sessionRepository
                .findByOwnerIdAndBoardIdOrderByUpdatedAtDesc(ownerId, boardId).stream()
                .map(SessionSummary::of)
                .toList();
        return new SessionListResponse(sessions, sessions.size());
    }

    public SessionDetailResponse getSession(String id, Integer ownerId) {
        return SessionDetailResponse.of(load(id, ownerId));
    }

    public void deleteSession(String id, Integer ownerId) {
        sessionRepository.delete(load(id, ownerId));
    }

    public void setActionStatus(String id, ActionStatusRequest request, Integer ownerId) {
        String status = request.status();
        if (!List.of("pending", "applied", "rejected").contains(status)) {
            throw new BadRequestException("status must be pending|applied|rejected");
        }
        ChatSession session = load(id, ownerId);
        List<StoredMessage> messages = session.getMessages();
        StoredMessage message = at(messages, request.messageIndex());
        StoredAction action = at(message == null ? null : message.getActions(), request.actionIndex());
        if (action == null) {
            throw new NotFoundException("No action at message " + request.messageIndex()
                    + " / action " + request.actionIndex());
        }
        action.setStatus(status);
        session.setUpdatedAt(Instant.now());
        sessionRepository.save(session);
    }

    // --- prompt / parsing ---

    private String systemPrompt() {
        return """
                You are an assistant embedded in a visual board of sticky notes. Reply to the user and,
                when helpful, propose board actions. Return a SINGLE JSON object and nothing else
                (no markdown fences):

                {
                  "message": string,                 // your reply to the user (markdown allowed)
                  "actions": [                        // may be []
                    {
                      "type": "create_note" | "update_note" | "move_note" | "delete_note",
                      "tempId": string,               // create_note only: a local handle (e.g. "t1")
                      "id": string,                   // update/move/delete: the note's server id
                      "content": { TiptapDoc },       // create/update: the note body as a TiptapDoc
                      "x": number, "y": number,       // create/move: world position
                      "width": number, "height": number, // create: size
                      "reason": string                // short rationale shown to the user
                    }
                  ]
                }

                Only set fields relevant to each action type. Use note ids exactly as given in the
                provided context for update/move/delete. Propose actions only when the user clearly
                wants the board changed; otherwise return "actions": []. Return valid JSON only.
                """;
    }

    private String buildContext(List<NoteContext> selection, String selectedText) {
        StringBuilder sb = new StringBuilder();
        if (selection != null && !selection.isEmpty()) {
            sb.append("Selected notes (context):\n");
            for (NoteContext n : selection) {
                String text = TiptapText.toPlainText(n.content());
                sb.append("--- NOTE id=").append(n.id());
                if (n.hasGeometry()) {
                    sb.append(" pos(").append(num(n.x())).append(",").append(num(n.y())).append(")");
                    if (n.width() != null && n.height() != null) {
                        sb.append(" size ").append(num(n.width())).append("x").append(num(n.height()));
                    }
                }
                sb.append(" ---\n").append(text.isBlank() ? "(empty)" : text).append("\n\n");
            }
        }
        if (selectedText != null && !selectedText.isBlank()) {
            sb.append("Highlighted text the user is referring to:\n").append(selectedText.strip()).append("\n");
        }
        return sb.toString();
    }

    private String num(Double d) {
        if (d == null) {
            return "?";
        }
        return d == Math.floor(d) ? String.valueOf((long) (double) d) : String.valueOf(d);
    }

    private Parsed parse(String content) {
        try {
            JsonNode root = objectMapper.readTree(stripFences(content));
            String message = root.path("message").asText("");
            List<BoardAction> actions = new ArrayList<>();
            JsonNode arr = root.path("actions");
            if (arr.isArray()) {
                for (JsonNode node : arr) {
                    actions.add(objectMapper.convertValue(node, BoardAction.class));
                }
            }
            // A model that ignored the schema still gives the user its raw text.
            if (message.isBlank() && actions.isEmpty()) {
                message = content == null ? "" : content.strip();
            }
            return new Parsed(message, actions);
        } catch (Exception e) {
            return new Parsed(content == null ? "" : content.strip(), List.of());
        }
    }

    private String stripFences(String content) {
        String trimmed = content == null ? "" : content.strip();
        if (trimmed.startsWith("```")) {
            int firstBreak = trimmed.indexOf('\n');
            if (firstBreak >= 0) {
                trimmed = trimmed.substring(firstBreak + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.strip();
    }

    // --- helpers ---

    private ChatSession resolveSession(String sessionId, String boardId, Integer ownerId) {
        if (sessionId == null) {
            return ChatSession.builder()
                    .ownerId(ownerId)
                    .boardId(boardId)
                    .provider(null)
                    .messages(new ArrayList<>())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }
        ChatSession session = load(sessionId, ownerId);
        if (session.getMessages() == null) {
            session.setMessages(new ArrayList<>());
        }
        return session;
    }

    private ChatSession load(String id, Integer ownerId) {
        return sessionRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new NotFoundException("Chat session not found: " + id));
    }

    private List<StoredAction> toStoredActions(List<BoardAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return null;
        }
        List<StoredAction> out = new ArrayList<>(actions.size());
        for (BoardAction a : actions) {
            out.add(new StoredAction(a, "pending"));
        }
        return out;
    }

    private ChatMessage lastUserMessage(List<ChatMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).role())) {
                return messages.get(i);
            }
        }
        return null;
    }

    private String deriveTitle(List<ChatMessage> messages) {
        String first = messages.stream()
                .filter(m -> "user".equals(m.role()))
                .map(ChatMessage::content)
                .filter(c -> c != null && !c.isBlank())
                .findFirst()
                .orElse("New conversation")
                .strip();
        return first.length() <= TITLE_MAX ? first : first.substring(0, TITLE_MAX).strip() + "…";
    }

    private <T> T at(List<T> list, Integer index) {
        if (list == null || index == null || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /** Parsed assistant output. */
    private record Parsed(String message, List<BoardAction> actions) {
    }
}
