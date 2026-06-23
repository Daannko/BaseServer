package dev.dankoz.BaseServer.app.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ImageCreateRequest(
    @NotNull Integer x,
    @NotNull Integer y,
    @NotNull Integer width,
    @NotNull Integer height,

    @NotBlank String src,
    Integer naturalWidth,
    Integer naturalHeight,

    Integer zIndex,
    String bgColor,
    String borderColor,
    Integer borderWidth
) {}
