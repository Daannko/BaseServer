package dev.dankoz.BaseServer.ai;

import java.util.List;

import dev.dankoz.BaseServer.ai.dto.ActionStatusRequest;
import dev.dankoz.BaseServer.ai.dto.BoardChatRequest;
import dev.dankoz.BaseServer.ai.dto.BoardChatResponse;
import dev.dankoz.BaseServer.ai.dto.ChatMessage;
import dev.dankoz.BaseServer.ai.dto.ChatRequest;
import dev.dankoz.BaseServer.ai.dto.ChatResponse;
import dev.dankoz.BaseServer.ai.dto.SessionDetailResponse;
import dev.dankoz.BaseServer.ai.dto.SessionListResponse;
import dev.dankoz.BaseServer.general.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;
    private final BoardChatService boardChatService;
    private final UserService userService;

    public AiController(AiService aiService, BoardChatService boardChatService, UserService userService) {
        this.aiService = aiService;
        this.boardChatService = boardChatService;
        this.userService = userService;
    }

    /**
     * Board-aware chat. Returns the assistant message + proposed board actions and persists
     * the turn into a per-board session. Omit {@code sessionId} to start a new session.
     */
    @PostMapping("/chat")
    public ResponseEntity<BoardChatResponse> chat(@RequestBody BoardChatRequest request) {
        return ResponseEntity.ok(boardChatService.chat(request, getUserId()));
    }

    /** Low-level session-aware chat against an explicitly named provider (no board actions). */
    @PostMapping("/chat/{provider}")
    public ResponseEntity<ChatResponse> chat(@PathVariable String provider,
                                             @RequestBody ChatRequest request) {
        return ResponseEntity.ok(aiService.chat(provider, request, getUserId()));
    }

    /** List board conversations (newest first, no messages). */
    @GetMapping("/chat/sessions")
    public ResponseEntity<SessionListResponse> sessions(@RequestParam String boardId) {
        return ResponseEntity.ok(boardChatService.listSessions(boardId, getUserId()));
    }

    /** One conversation with messages + per-action status. */
    @GetMapping("/chat/sessions/{id}")
    public ResponseEntity<SessionDetailResponse> session(@PathVariable String id) {
        return ResponseEntity.ok(boardChatService.getSession(id, getUserId()));
    }

    /** Delete a conversation. */
    @DeleteMapping("/chat/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable String id) {
        boardChatService.deleteSession(id, getUserId());
        return ResponseEntity.noContent().build();
    }

    /** Persist an action resolution (apply/reject/revert) by its position in the conversation. */
    @PatchMapping("/chat/sessions/{id}/action-status")
    public ResponseEntity<Void> actionStatus(@PathVariable String id,
                                             @RequestBody ActionStatusRequest request) {
        boardChatService.setActionStatus(id, request, getUserId());
        return ResponseEntity.noContent().build();
    }

    /** Fetch a stored session's raw message history (owner-scoped, provider-wire view). */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<ChatMessage>> history(@PathVariable String sessionId) {
        return ResponseEntity.ok(aiService.getHistory(sessionId, getUserId()));
    }

    private Integer getUserId() {
        return userService.getCurrentUser().getId();
    }
}
