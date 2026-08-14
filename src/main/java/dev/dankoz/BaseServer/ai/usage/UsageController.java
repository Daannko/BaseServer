package dev.dankoz.BaseServer.ai.usage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.dankoz.BaseServer.ai.usage.dto.UsageResponse;
import dev.dankoz.BaseServer.general.service.UserService;

@RestController
@RequestMapping("/ai/usage")
public class UsageController {

    private final TokenUsageService tokenUsageService;
    private final UserService userService;

    public UsageController(TokenUsageService tokenUsageService, UserService userService) {
        this.tokenUsageService = tokenUsageService;
        this.userService = userService;
    }

    /** Total AI token usage for the current user, with per-feature and per-model breakdowns. */
    @GetMapping
    public ResponseEntity<UsageResponse> myUsage() {
        return ResponseEntity.ok(tokenUsageService.getUsage(userService.getCurrentUser().getId()));
    }
}
