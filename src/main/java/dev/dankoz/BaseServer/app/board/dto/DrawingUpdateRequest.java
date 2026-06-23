package dev.dankoz.BaseServer.app.board.dto;

import java.util.List;

import dev.dankoz.BaseServer.app.board.model.Stroke;

public record DrawingUpdateRequest(
    Integer x, Integer y, Integer width, Integer height,
    Integer zIndex, String bgColor, String borderColor, Integer borderWidth,

    List<Stroke> strokes
) {}
