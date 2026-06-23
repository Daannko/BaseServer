package dev.dankoz.BaseServer.app.board.dto;

import java.util.List;

import dev.dankoz.BaseServer.app.board.model.Stroke;
import jakarta.validation.constraints.NotNull;

public record DrawingCreateRequest(
    @NotNull Integer x,
    @NotNull Integer y,
    @NotNull Integer width,
    @NotNull Integer height,

    List<Stroke> strokes,

    Integer zIndex,
    String bgColor,
    String borderColor,
    Integer borderWidth
) {}
