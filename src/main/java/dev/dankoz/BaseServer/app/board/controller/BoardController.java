package dev.dankoz.BaseServer.app.board.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.dankoz.BaseServer.app.board.dto.AddBoardRequestDTO;
import dev.dankoz.BaseServer.app.board.dto.BoardDTOMapper;
import dev.dankoz.BaseServer.app.board.dto.BoardElementsResponse;
import dev.dankoz.BaseServer.app.board.dto.GetBoardInfoReponse;
import dev.dankoz.BaseServer.app.board.dto.UpdateBoardRequestDTO;
import dev.dankoz.BaseServer.app.board.model.Board;
import dev.dankoz.BaseServer.app.board.model.DrawingElement;
import dev.dankoz.BaseServer.app.board.model.Element;
import dev.dankoz.BaseServer.app.board.model.ImageElement;
import dev.dankoz.BaseServer.app.board.model.NoteElement;
import dev.dankoz.BaseServer.app.board.model.SectionElement;
import dev.dankoz.BaseServer.app.board.service.BoardService;
import dev.dankoz.BaseServer.app.board.service.ElementService;
import dev.dankoz.BaseServer.general.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;
    private final BoardDTOMapper boardDTOMapper;
    private final ElementService elementService;

    public BoardController(BoardService boardService, UserService userService, BoardDTOMapper boardDTOMapper,
            ElementService elementService) {
        this.boardService = boardService;
        this.userService = userService;
        this.boardDTOMapper = boardDTOMapper;
        this.elementService = elementService;
    }

    @PostMapping
    public Board addBoard(@Valid @RequestBody AddBoardRequestDTO requestDTO) {
        return boardService.addBoard(
                requestDTO.name(),
                requestDTO.description(),
                requestDTO.width(),
                requestDTO.height(),
                getUserId());
    }

    @DeleteMapping("/{id}")
    public void removeBoard(@PathVariable String id) {
        boardService.removeBoard(id, getUserId());
    }

    @PatchMapping("/{id}")
    public void updateBoard(@PathVariable String id, @Valid @RequestBody UpdateBoardRequestDTO requestDTO) {
        boardService.updateBoard(id, requestDTO, getUserId());
    }

    @GetMapping("/all")
    public List<GetBoardInfoReponse> getAllBoards() {
        return boardService.getAllBoards(getUserId())
                .stream()
                .map(boardDTOMapper::toGetAllBoardsInfoResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public Board getBoard(@PathVariable String id) {
        return boardService.getBoard(id, getUserId());
    }

    @GetMapping("/{id}/elements")
    public BoardElementsResponse getBoardElements(@PathVariable String id) {
        List<Element> elements = elementService.getAllByBoard(id, getUserId());

        List<NoteElement> notes = new ArrayList<>();
        List<SectionElement> sections = new ArrayList<>();
        List<ImageElement> images = new ArrayList<>();
        List<DrawingElement> drawings = new ArrayList<>();

        for (Element e : elements) {
            if (e instanceof NoteElement n) notes.add(n);
            else if (e instanceof SectionElement s) sections.add(s);
            else if (e instanceof ImageElement i) images.add(i);
            else if (e instanceof DrawingElement d) drawings.add(d);
        }
        return new BoardElementsResponse(notes, sections, images, drawings);
    }

    private Integer getUserId() {
        return userService.getCurrentUser().getId();
    }
}
