package dev.dankoz.BaseServer.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.dankoz.BaseServer.ai.dto.ChatMessage;
import dev.dankoz.BaseServer.ai.dto.ChatRequest;
import dev.dankoz.BaseServer.ai.dto.ChatResponse;
import dev.dankoz.BaseServer.ai.dto.TokenUsage;
import dev.dankoz.BaseServer.config.properties.AiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * DeepSeek provider. Uses the OpenAI-compatible {@code /chat/completions} endpoint
 * with Bearer auth. Model comes from config ({@code deepseek-v4-pro} default; cheap
 * {@code deepseek-v4-flash} for tag extraction) or a per-request override.
 */
@Component
public class DeepSeekProvider implements AiProvider {

    private final AiProperties.Deepseek config;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeepSeekProvider(AiProperties aiProperties) {


        this.config = aiProperties.deepseek();
        this.restClient = RestClient.builder()
                .baseUrl(config.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String name() {
        return "deepseek";
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        String model = request.model() != null ? request.model() : config.model();

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("stream", false);
        ArrayNode messages = body.putArray("messages");
        for (ChatMessage message : request.messages()) {
            ObjectNode node = messages.addObject();
            node.put("role", message.role());
            node.put("content", message.content());
        }

        // Optional tuning params — only sent when set.
        if (request.temperature() != null) {
            body.put("temperature", request.temperature());
        }
        if (request.topP() != null) {
            body.put("top_p", request.topP());
        }
        if (request.maxTokens() != null) {
            body.put("max_tokens", request.maxTokens());
        }
        if (request.responseFormat() != null) {
            body.putObject("response_format").put("type", request.responseFormat());
        }
        // Passthrough for params not modeled above.
        if (request.extra() != null) {
            body.setAll((ObjectNode) objectMapper.valueToTree(request.extra()));
        }

        JsonNode response = restClient.post()
                .uri("/chat/completions")
                .body(body.toString())
                .retrieve()
                .body(JsonNode.class);

        String content = response
                .path("choices").path(0).path("message").path("content").asText();
        String usedModel = response.path("model").asText(model);

        JsonNode usageNode = response.path("usage");
        TokenUsage usage = new TokenUsage(
                usageNode.path("prompt_tokens").asInt(0),
                usageNode.path("completion_tokens").asInt(0),
                usageNode.path("total_tokens").asInt(0));

        return new ChatResponse(content, usedModel, name(), usage);
    }
}
