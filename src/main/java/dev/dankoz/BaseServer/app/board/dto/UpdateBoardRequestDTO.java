package dev.dankoz.BaseServer.app.board.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateBoardRequestDTO(

    @NotNull(message = "Board ID is required")
    @Positive(message = "Board ID must be positive")
    String boardId,

    @Size(max = 255, message = "Board name is too long")
    String name,

    @Size(max = 1000, message = "Board name is too long")
    String description 

) {

}
