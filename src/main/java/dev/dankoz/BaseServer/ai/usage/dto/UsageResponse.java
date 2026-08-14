package dev.dankoz.BaseServer.ai.usage.dto;

import java.util.List;

/** A user's total AI token usage with breakdowns. */
public record UsageResponse(
        long totalTokens,
        long promptTokens,
        long completionTokens,
        long requests,
        List<UsageBucket> byFeature,
        List<UsageBucket> byModel) {
}
