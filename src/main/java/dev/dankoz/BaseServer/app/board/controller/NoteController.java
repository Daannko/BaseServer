package dev.dankoz.BaseServer.app.board.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.dankoz.BaseServer.app.board.dto.NoteCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.NoteUpdateRequest;
import dev.dankoz.BaseServer.app.board.model.NoteElement;
import dev.dankoz.BaseServer.app.board.service.ElementService;
import dev.dankoz.BaseServer.general.service.UserService;
import jakarta.validation.Valid;

@RestController
public class NoteController {

    private final ElementService elementService;
    private final UserService userService;

    public NoteController(ElementService elementService, UserService userService) {
        this.elementService = elementService;
        this.userService = userService;
    }

    @PostMapping("/board/{boardId}/note")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteElement create(@PathVariable String boardId, @Valid @RequestBody NoteCreateRequest req) {
        return elementService.createNote(req, boardId, getUserId());
    }

    @PatchMapping("/note/{id}")
    public NoteElement update(@PathVariable String id, @Valid @RequestBody NoteUpdateRequest req) {
        return elementService.updateNote(id, req, getUserId());
    }

    @DeleteMapping("/note/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        elementService.deleteElement(id, getUserId());
    }

    private Integer getUserId() {
        return userService.getCurrentUser().getId();
    }
}
