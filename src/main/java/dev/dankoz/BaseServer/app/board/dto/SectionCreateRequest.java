package dev.dankoz.BaseServer.app.board.dto;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.dankoz.BaseServer.app.board.jackson.TiptapDocMapDeserializer;
import dev.dankoz.BaseServer.app.board.validation.TiptapDoc;
import jakarta.validation.constraints.NotNull;

public record SectionCreateRequest(
    @NotNull Integer x,
    @NotNull Integer y,
    @NotNull Integer width,
    @NotNull Integer height,

    @TiptapDoc
    @JsonDeserialize(using = TiptapDocMapDeserializer.class)
    Map<String, Object> title,

    Integer zIndex,
    String bgColor,
    String borderColor,
    Integer borderWidth
) {}
