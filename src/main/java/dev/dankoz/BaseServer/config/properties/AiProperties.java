package dev.dankoz.BaseServer.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI configuration. {@code provider} selects the default provider used by
 * {@link dev.dankoz.BaseServer.ai.AiService}. Add a new nested record + provider
 * impl to support more providers later.
 */
@ConfigurationProperties(prefix = "ai")
public record AiProperties(String provider, Deepseek deepseek) {

    /**
     * @param tagModel cheap model used for lightweight tag/topic extraction; falls back to
     *                 {@code model} when blank.
     */
    public record Deepseek(String apiKey, String baseUrl, String model, String tagModel) {
    }
}
