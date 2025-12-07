package dev.dankoz.BaseServer.app.board.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddBoardRequestDTO(
    @NotBlank( message = "Board name is required")
    @Size(max = 255, message = "Board name is too long")
    String name,

    @Size(max = 1000, message = "Board name is too long")
    String description 

) {}
