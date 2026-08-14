package dev.dankoz.BaseServer.ai.quiz.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.dankoz.BaseServer.ai.quiz.dto.EvaluateRequest;
import dev.dankoz.BaseServer.ai.quiz.dto.EvaluateResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.ExtractTagsRequest;
import dev.dankoz.BaseServer.ai.quiz.dto.ExtractTagsResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.GenerateQuizRequest;
import dev.dankoz.BaseServer.ai.quiz.dto.OneOffEvaluateRequest;
import dev.dankoz.BaseServer.ai.quiz.dto.QuizListResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.QuizResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.TagListResponse;
import dev.dankoz.BaseServer.ai.quiz.dto.UpdateQuizRequest;
import dev.dankoz.BaseServer.ai.quiz.service.QuizService;
import dev.dankoz.BaseServer.general.service.UserService;

@RestController
@RequestMapping("/ai/quiz")
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    public QuizController(QuizService quizService, UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }

    /** Generate (and optionally store) a quiz from selected notes. Taking-mode response (no answer keys). */
    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generate(@RequestBody GenerateQuizRequest request) {
        return ResponseEntity.ok(quizService.generate(request, getUserId()));
    }

    /**
     * Extract (or reuse cached) general topic tags for the given notes. First action of the
     * "extract tags" flow; tags are persisted per note and reused while the note is unchanged.
     */
    @PostMapping("/extract-tags")
    public ResponseEntity<ExtractTagsResponse> extractTags(@RequestBody ExtractTagsRequest request) {
        return ResponseEntity.ok(quizService.extractTags(request, getUserId()));
    }

    /** List stored quizzes (summaries). Filters optional; {@code noteId}/{@code tag} repeatable. */
    @GetMapping
    public ResponseEntity<QuizListResponse> list(
            @RequestParam(required = false) String boardId,
            @RequestParam(required = false) List<String> noteId,
            @RequestParam(required = false) List<String> tag,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(quizService.list(boardId, noteId, tag, q, limit, offset, getUserId()));
    }

    /** Distinct quiz-level tags with counts. */
    @GetMapping("/tags")
    public ResponseEntity<TagListResponse> tags() {
        return ResponseEntity.ok(quizService.listTags(getUserId()));
    }

    /** Fetch one quiz. {@code reveal=true} (owner) includes answer keys + explanations. */
    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> get(@PathVariable String id,
                                            @RequestParam(defaultValue = "false") boolean reveal) {
        return ResponseEntity.ok(quizService.get(id, getUserId(), reveal));
    }

    /** Partial update: title, tags, and/or full question replacement. Returns reveal shape. */
    @PatchMapping("/{id}")
    public ResponseEntity<QuizResponse> update(@PathVariable String id,
                                               @RequestBody UpdateQuizRequest request) {
        return ResponseEntity.ok(quizService.update(id, request, getUserId()));
    }

    /** Backfill per-option explanations for all closed questions of a stored quiz. Returns reveal shape. */
    @PostMapping("/{id}/explain-options")
    public ResponseEntity<QuizResponse> explainOptions(@PathVariable String id) {
        return ResponseEntity.ok(quizService.explainOptions(id, getUserId()));
    }

    /** Backfill per-option explanations for a single question of a stored quiz. Returns reveal shape. */
    @PostMapping("/{id}/questions/{questionId}/explain-options")
    public ResponseEntity<QuizResponse> explainQuestionOptions(@PathVariable String id,
                                                               @PathVariable String questionId) {
        return ResponseEntity.ok(quizService.explainQuestionOptions(id, questionId, getUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        quizService.delete(id, getUserId());
        return ResponseEntity.noContent().build();
    }

    /** Evaluate answers against a stored quiz. */
    @PostMapping("/{id}/evaluate")
    public ResponseEntity<EvaluateResponse> evaluate(@PathVariable String id,
                                                     @RequestBody EvaluateRequest request) {
        return ResponseEntity.ok(quizService.evaluateStored(id, request, getUserId()));
    }

    /** Evaluate answers for a one-off (unstored) quiz — questions sent back in the body. */
    @PostMapping("/evaluate")
    public ResponseEntity<EvaluateResponse> evaluateOneOff(@RequestBody OneOffEvaluateRequest request) {
        return ResponseEntity.ok(quizService.evaluateOneOff(request, getUserId()));
    }

    private Integer getUserId() {
        return userService.getCurrentUser().getId();
    }
}
