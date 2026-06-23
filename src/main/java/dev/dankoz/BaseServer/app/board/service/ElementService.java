package dev.dankoz.BaseServer.app.board.service;

import java.util.List;

import dev.dankoz.BaseServer.app.board.dto.DrawingCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.DrawingUpdateRequest;
import dev.dankoz.BaseServer.app.board.dto.ImageCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.ImageUpdateRequest;
import dev.dankoz.BaseServer.app.board.dto.NoteCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.NoteUpdateRequest;
import dev.dankoz.BaseServer.app.board.dto.SectionCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.SectionUpdateRequest;
import dev.dankoz.BaseServer.app.board.model.DrawingElement;
import dev.dankoz.BaseServer.app.board.model.Element;
import dev.dankoz.BaseServer.app.board.model.ImageElement;
import dev.dankoz.BaseServer.app.board.model.NoteElement;
import dev.dankoz.BaseServer.app.board.model.SectionElement;

public interface ElementService {

    NoteElement createNote(NoteCreateRequest req, String boardId, Integer authorId);
    SectionElement createSection(SectionCreateRequest req, String boardId, Integer authorId);
    ImageElement createImage(ImageCreateRequest req, String boardId, Integer authorId);
    DrawingElement createDrawing(DrawingCreateRequest req, String boardId, Integer authorId);

    NoteElement updateNote(String id, NoteUpdateRequest req, Integer authorId);
    SectionElement updateSection(String id, SectionUpdateRequest req, Integer authorId);
    ImageElement updateImage(String id, ImageUpdateRequest req, Integer authorId);
    DrawingElement updateDrawing(String id, DrawingUpdateRequest req, Integer authorId);

    void deleteElement(String id, Integer authorId);

    List<Element> getAllByBoard(String boardId, Integer authorId);

    Element getElement(String id, Integer authorId);
}
