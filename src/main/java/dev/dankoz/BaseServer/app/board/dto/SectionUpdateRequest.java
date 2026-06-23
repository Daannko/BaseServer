package dev.dankoz.BaseServer.app.board.dto;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.dankoz.BaseServer.app.board.jackson.TiptapDocMapDeserializer;
import dev.dankoz.BaseServer.app.board.validation.TiptapDoc;

public record SectionUpdateRequest(
    Integer x, Integer y, Integer width, Integer height,
    Integer zIndex, String bgColor, String borderColor, Integer borderWidth,

    @TiptapDoc
    @JsonDeserialize(using = TiptapDocMapDeserializer.class)
    Map<String, Object> title
) {}
