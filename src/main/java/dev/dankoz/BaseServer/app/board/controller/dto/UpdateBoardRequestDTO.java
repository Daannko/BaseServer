package dev.dankoz.BaseServer.app.board.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateBoardRequestDTO(

    @NotNull(message = "Board ID is required")
    @Positive(message = "Board ID must be positive")
    Integer boardId,

    @Size(max = 255, message = "Board name is too long")
    String name,

    @Size(max = 1000, message = "Board name is too long")
    String description 

) {

}
