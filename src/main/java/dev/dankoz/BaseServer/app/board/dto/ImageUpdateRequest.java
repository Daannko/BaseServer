package dev.dankoz.BaseServer.app.board.dto;

public record ImageUpdateRequest(
    Integer x, Integer y, Integer width, Integer height,
    Integer zIndex, String bgColor, String borderColor, Integer borderWidth,

    String src,
    Integer naturalWidth,
    Integer naturalHeight
) {}
