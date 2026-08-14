package dev.dankoz.BaseServer.app.board.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import dev.dankoz.BaseServer.app.board.dto.DrawingCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.DrawingUpdateRequest;
import dev.dankoz.BaseServer.app.board.dto.ImageCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.ImageUpdateRequest;
import dev.dankoz.BaseServer.app.board.dto.NoteCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.NoteUpdateRequest;
import dev.dankoz.BaseServer.app.board.dto.SectionCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.SectionUpdateRequest;
import dev.dankoz.BaseServer.app.board.model.Board;
import dev.dankoz.BaseServer.app.board.model.DrawingElement;
import dev.dankoz.BaseServer.app.board.model.Element;
import dev.dankoz.BaseServer.app.board.model.ImageElement;
import dev.dankoz.BaseServer.app.board.model.NoteElement;
import dev.dankoz.BaseServer.app.board.model.SectionElement;
import dev.dankoz.BaseServer.app.board.model.Stroke;
import dev.dankoz.BaseServer.app.board.repository.ElementRepository;
import dev.dankoz.BaseServer.app.board.service.BoardService;
import dev.dankoz.BaseServer.app.board.service.ElementService;
import dev.dankoz.BaseServer.app.board.storage.ImageStore;
import dev.dankoz.BaseServer.app.board.util.TiptapDocs;

import dev.dankoz.BaseServer.config.exceptions.BadRequestException;
import dev.dankoz.BaseServer.config.exceptions.UnauthorizedException;

@Service
public class ElementServiceImpl implements ElementService {

    private final ElementRepository elementRepository;
    private final BoardService boardService;
    private final ImageStore imageStore;

    public ElementServiceImpl(ElementRepository elementRepository, BoardService boardService, ImageStore imageStore) {
        this.elementRepository = elementRepository;
        this.boardService = boardService;
        this.imageStore = imageStore;
    }

    // ── create ────────────────────────────────────────────────────────

    @Override
    public NoteElement createNote(NoteCreateRequest req, String boardId, Integer authorId) {
        Board board = boardService.getBoard(boardId, authorId);

        NoteElement e = new NoteElement();
        applyCreateBase(e, boardId, authorId, req.x(), req.y(), req.width(), req.height(),
                req.zIndex(), req.bgColor(), req.borderColor(), req.borderWidth());
        e.setContent(req.content() != null ? req.content() : TiptapDocs.emptyDoc());
        e.setFontSize(req.fontSize() != null ? req.fontSize() : 25);
        e.setPadding(req.padding());
        insert(e);
        addToBoard(board, e.getId());
        return e;
    }

    @Override
    public SectionElement createSection(SectionCreateRequest req, String boardId, Integer authorId) {
        Board board = boardService.getBoard(boardId, authorId);

        SectionElement e = new SectionElement();
        applyCreateBase(e, boardId, authorId, req.x(), req.y(), req.width(), req.height(),
                req.zIndex(), req.bgColor(), req.borderColor(), req.borderWidth());
        e.setTitle(req.title() != null ? req.title() : TiptapDocs.emptyDoc());
        insert(e);
        addToBoard(board, e.getId());
        return e;
    }

    @Override
    public ImageElement createImage(ImageCreateRequest req, String boardId, Integer authorId) {
        Board board = boardService.getBoard(boardId, authorId);

        ImageElement e = new ImageElement();
        applyCreateBase(e, boardId, authorId, req.x(), req.y(), req.width(), req.height(),
                req.zIndex(), req.bgColor(), req.borderColor(), req.borderWidth());
        e.setSrc(req.src());
        e.setNaturalWidth(req.naturalWidth() != null ? req.naturalWidth() : 0);
        e.setNaturalHeight(req.naturalHeight() != null ? req.naturalHeight() : 0);
        insert(e);
        addToBoard(board, e.getId());
        return e;
    }

    @Override
    public DrawingElement createDrawing(DrawingCreateRequest req, String boardId, Integer authorId) {
        Board board = boardService.getBoard(boardId, authorId);

        DrawingElement e = new DrawingElement();
        applyCreateBase(e, boardId, authorId, req.x(), req.y(), req.width(), req.height(),
                req.zIndex(), req.bgColor(), req.borderColor(), req.borderWidth());
        e.setStrokes(req.strokes() != null ? new ArrayList<>(req.strokes()) : new ArrayList<>());
        insert(e);
        addToBoard(board, e.getId());
        return e;
    }

    // ── update ───────────────────────────────────────────────────────

    @Override
    public NoteElement updateNote(String id, NoteUpdateRequest req, Integer authorId) {
        Element raw = getOwned(id, authorId);
        if (!(raw instanceof NoteElement e)) {
            throw new BadRequestException("Element is not a NOTE");
        }
        applyUpdateBase(e, req.x(), req.y(), req.width(), req.height(),
                req.zIndex(), req.bgColor(), req.borderColor(), req.borderWidth());
        if (req.content() != null && !req.content().isEmpty()) {
            e.setContent(req.content());
            // Content changed => any extracted topic tags are stale; wipe so they're re-extracted on demand.
            e.setTags(null);
            e.setTagsHash(null);
        }
        if (req.fontSize() != null) {
            e.setFontSize(req.fontSize());
        }
        if (req.padding() != null) {
            e.setPadding(req.padding());
        }
        elementRepository.save(e);
        return e;
    }

    @Override
    public SectionElement updateSection(String id, SectionUpdateRequest req, Integer authorId) {
        Element raw = getOwned(id, authorId);
        if (!(raw instanceof SectionElement e)) {
            throw new BadRequestException("Element is not a SECTION");
        }
        applyUpdateBase(e, req.x(), req.y(), req.width(), req.height(),
                req.zIndex(), req.bgColor(), req.borderColor(), req.borderWidth());
        if (req.title() != null && !req.title().isEmpty()) {
            e.setTitle(req.title());
        }
        elementRepository.save(e);
        return e;
    }

    @Override
    public ImageElement updateImage(String id, ImageUpdateRequest req, Integer authorId) {
        Element raw = getOwned(id, authorId);
        if (!(raw instanceof ImageElement e)) {
            throw new BadRequestException("Element is not an IMAGE");
        }
        applyUpdateBase(e, req.x(), req.y(), req.width(), req.height(),
                req.zIndex(), req.bgColor(), req.borderColor(), req.borderWidth());
        if (req.src() != null) {
            e.setSrc(req.src());
        }
        if (req.naturalWidth() != null) {
            e.setNaturalWidth(req.naturalWidth());
        }
        if (req.naturalHeight() != null) {
            e.setNaturalHeight(req.naturalHeight());
        }
        elementRepository.save(e);
        return e;
    }

    @Override
    public DrawingElement updateDrawing(String id, DrawingUpdateRequest req, Integer authorId) {
        Element raw = getOwned(id, authorId);
        if (!(raw instanceof DrawingElement e)) {
            throw new BadRequestException("Element is not a DRAWING");
        }
        applyUpdateBase(e, req.x(), req.y(), req.width(), req.height(),
                req.zIndex(), req.bgColor(), req.borderColor(), req.borderWidth());
        if (req.strokes() != null) {
            e.setStrokes(new ArrayList<>(req.strokes()));
        }
        elementRepository.save(e);
        return e;
    }

    // ── delete ───────────────────────────────────────────────────────

    @Override
    public void deleteElement(String id, Integer authorId) {
        Element e = getOwned(id, authorId);

        // Image lifecycle: delete backing blob synchronously.
        if (e instanceof ImageElement img) {
            String src = img.getSrc();
            if (src != null) {
                imageStore.deleteByReference(src);
            }
        }

        Board board = boardService.getBoard(e.getBoardId(), authorId);
        board.removeTopic(e.getId());
        boardService.saveBoard(board);

        elementRepository.delete(e);
    }

    // ── queries ──────────────────────────────────────────────────────

    @Override
    public List<Element> getAllByBoard(String boardId, Integer authorId) {
        boardService.getBoard(boardId, authorId);
        return elementRepository.findAllByBoardId(boardId);
    }

    @Override
    public Element getElement(String id, Integer authorId) {
        return getOwned(id, authorId);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Element getOwned(String id, Integer authorId) {
        Optional<Element> opt = elementRepository.findById(id);
        if (opt.isEmpty() || !Objects.equals(opt.get().getAuthorId(), authorId)) {
            throw new UnauthorizedException("The element does not exist or you do not have access to it");
        }
        return opt.get();
    }

    private void applyCreateBase(Element e, String boardId, Integer authorId,
            Integer x, Integer y, Integer width, Integer height,
            Integer zIndex, String bgColor, String borderColor, Integer borderWidth) {
        e.setBoardId(boardId);
        e.setAuthorId(authorId);
        e.setX(x);
        e.setY(y);
        e.setWidth(width);
        e.setHeight(height);
        e.setZIndex(zIndex != null ? zIndex : 1);
        e.setBgColor(bgColor);
        e.setBorderColor(borderColor);
        e.setBorderWidth(borderWidth != null ? borderWidth : 1);
    }

    private void applyUpdateBase(Element e,
            Integer x, Integer y, Integer width, Integer height,
            Integer zIndex, String bgColor, String borderColor, Integer borderWidth) {
        if (x != null) e.setX(x);
        if (y != null) e.setY(y);
        if (width != null) e.setWidth(width);
        if (height != null) e.setHeight(height);
        if (zIndex != null) e.setZIndex(zIndex);
        if (bgColor != null) e.setBgColor(bgColor);
        if (borderColor != null) e.setBorderColor(borderColor);
        if (borderWidth != null) e.setBorderWidth(borderWidth);
    }

    private void addToBoard(Board board, String elementId) {
        board.addTopic(elementId);
        boardService.saveBoard(board);
    }

    private void insert(Element e) {
        e.setId(new ObjectId().toHexString());
        int attempts = 0;
        while (true) {
            try {
                elementRepository.insert(e);
                break;
            } catch (DuplicateKeyException ex) {
                attempts++;
                if (attempts >= 3) throw ex;
                e.setId(new ObjectId().toHexString());
            }
        }
    }
}
