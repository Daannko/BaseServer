package dev.dankoz.BaseServer.app.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicRequestDTO(
    @Size(max = 255, message = "Topic title too long")
    String title,

    @Size(max = 100000, message = "Content too long")
    String content
) {}