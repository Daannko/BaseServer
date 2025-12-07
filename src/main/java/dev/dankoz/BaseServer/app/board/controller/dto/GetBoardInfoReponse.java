package dev.dankoz.BaseServer.app.board.controller.dto;

public record GetBoardInfoReponse(
    Integer id,
    String name,
    String description
) {}
