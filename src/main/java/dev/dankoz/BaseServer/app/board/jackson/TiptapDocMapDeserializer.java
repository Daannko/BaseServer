package dev.dankoz.BaseServer.app.board.jackson;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TiptapDocMapDeserializer extends JsonDeserializer<Map<String, Object>> {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @Override
    public Map<String, Object> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectCodec codec = parser.getCodec();
        ObjectMapper mapper = codec instanceof ObjectMapper om ? om : new ObjectMapper();

        JsonNode node = codec.readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isTextual()) {
            String text = node.asText();
            if (text == null || text.isBlank()) {
                return null;
            }
            return mapper.readValue(text, MAP_TYPE);
        }

        return mapper.convertValue(node, MAP_TYPE);
    }
}
