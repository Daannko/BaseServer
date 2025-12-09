package dev.dankoz.BaseServer.app.board.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicCreateRequestDTO(
    @NotBlank(message = "Board ID is required")
    String boardId,

    @NotBlank(message = "Topic title is required")
    @Size(max = 255, message = "Topic title too long")
    String title,

    @Size(max = 5000, message = "Content too long")
    String content,

    List<String> relatedTopics
) {}