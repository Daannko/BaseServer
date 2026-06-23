package dev.dankoz.BaseServer.app.board.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.dankoz.BaseServer.app.board.dto.SectionCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.SectionUpdateRequest;
import dev.dankoz.BaseServer.app.board.model.SectionElement;
import dev.dankoz.BaseServer.app.board.service.ElementService;
import dev.dankoz.BaseServer.general.service.UserService;
import jakarta.validation.Valid;

@RestController
public class SectionController {

    private final ElementService elementService;
    private final UserService userService;

    public SectionController(ElementService elementService, UserService userService) {
        this.elementService = elementService;
        this.userService = userService;
    }

    @PostMapping("/board/{boardId}/section")
    @ResponseStatus(HttpStatus.CREATED)
    public SectionElement create(@PathVariable String boardId, @Valid @RequestBody SectionCreateRequest req) {
        return elementService.createSection(req, boardId, getUserId());
    }

    @PatchMapping("/section/{id}")
    public SectionElement update(@PathVariable String id, @Valid @RequestBody SectionUpdateRequest req) {
        return elementService.updateSection(id, req, getUserId());
    }

    @DeleteMapping("/section/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        elementService.deleteElement(id, getUserId());
    }

    private Integer getUserId() {
        return userService.getCurrentUser().getId();
    }
}
