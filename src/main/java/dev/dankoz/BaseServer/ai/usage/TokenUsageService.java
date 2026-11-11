package dev.dankoz.BaseServer.ai.usage;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.dankoz.BaseServer.ai.dto.ChatResponse;
import dev.dankoz.BaseServer.ai.dto.TokenUsage;
import dev.dankoz.BaseServer.ai.usage.dto.UsageBucket;
import dev.dankoz.BaseServer.ai.usage.dto.UsageResponse;
import dev.dankoz.BaseServer.ai.usage.model.TokenUsageRecord;
import dev.dankoz.BaseServer.ai.usage.repository.TokenUsageRepository;
import dev.dankoz.BaseServer.config.exceptions.AiLimitExceededException;

/** Records per-request token usage and reports per-user totals. */
@Service
public class TokenUsageService {

    private final TokenUsageRepository repository;
    private final long dailyTokenLimit;

    public TokenUsageService(TokenUsageRepository repository,
                             @Value("${app.ai.daily-token-limit}") long dailyTokenLimit) {
        this.repository = repository;
        this.dailyTokenLimit = dailyTokenLimit;
    }

    /** Rejects the call when the user spent the limit in the last 24 hours. */
    public void assertWithinDailyLimit(Integer userId) {
        Instant since = Instant.now().minus(Duration.ofDays(1));
        if (repository.sumTotalSince(userId, since) >= dailyTokenLimit) {
            throw new AiLimitExceededException("Daily AI limit reached, try again later");
        }
    }

    /** Persist usage from a completed AI call. No-ops when usage/user is missing. */
    public void record(Integer userId, ChatResponse response, String feature) {
        if (userId == null || response == null) {
            return;
        }
        TokenUsage usage = response.usage();
        if (usage == null) {
            return;
        }
        repository.save(TokenUsageRecord.builder()
                .userId(userId)
                .provider(response.provider())
                .model(response.model())
                .feature(feature)
                .promptTokens(usage.promptTokens())
                .completionTokens(usage.completionTokens())
                .totalTokens(usage.totalTokens())
                .createdAt(Instant.now())
                .build());
    }

    public UsageResponse getUsage(Integer userId) {
        Object[] totals = repository.totalsForUser(userId).stream().findFirst().orElse(null);
        long total = totals == null ? 0 : asLong(totals[0]);
        long prompt = totals == null ? 0 : asLong(totals[1]);
        long completion = totals == null ? 0 : asLong(totals[2]);
        long requests = totals == null ? 0 : asLong(totals[3]);

        return new UsageResponse(total, prompt, completion, requests,
                toBuckets(repository.sumByFeature(userId)),
                toBuckets(repository.sumByModel(userId)));
    }

    private List<UsageBucket> toBuckets(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new UsageBucket((String) r[0], asLong(r[1]), asLong(r[2])))
                .toList();
    }

    private long asLong(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }
}
