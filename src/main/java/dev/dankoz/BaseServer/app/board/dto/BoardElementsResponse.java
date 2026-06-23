package dev.dankoz.BaseServer.app.board.dto;

import java.util.List;

import dev.dankoz.BaseServer.app.board.model.DrawingElement;
import dev.dankoz.BaseServer.app.board.model.ImageElement;
import dev.dankoz.BaseServer.app.board.model.NoteElement;
import dev.dankoz.BaseServer.app.board.model.SectionElement;

public record BoardElementsResponse(
    List<NoteElement> notes,
    List<SectionElement> sections,
    List<ImageElement> images,
    List<DrawingElement> drawings
) {}
