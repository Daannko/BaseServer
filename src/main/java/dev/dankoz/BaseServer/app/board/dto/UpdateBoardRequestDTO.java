package dev.dankoz.BaseServer.app.board.dto;

import dev.dankoz.BaseServer.app.board.model.Camera;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Partial update. {@code id} comes from the PATCH path, not the body. */
public record UpdateBoardRequestDTO(
    @Size(max = 255, message = "Board name is too long")
    String name,

    @Size(max = 1000, message = "Board description is too long")
    String description,

    @Positive
    Integer width,

    @Positive
    Integer height,

    Camera camera
) {
}
