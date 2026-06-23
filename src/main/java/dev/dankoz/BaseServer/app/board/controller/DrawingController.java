package dev.dankoz.BaseServer.app.board.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.dankoz.BaseServer.app.board.dto.DrawingCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.DrawingUpdateRequest;
import dev.dankoz.BaseServer.app.board.model.DrawingElement;
import dev.dankoz.BaseServer.app.board.service.ElementService;
import dev.dankoz.BaseServer.general.service.UserService;
import jakarta.validation.Valid;

@RestController
public class DrawingController {

    private final ElementService elementService;
    private final UserService userService;

    public DrawingController(ElementService elementService, UserService userService) {
        this.elementService = elementService;
        this.userService = userService;
    }

    @PostMapping("/board/{boardId}/drawing")
    @ResponseStatus(HttpStatus.CREATED)
    public DrawingElement create(@PathVariable String boardId, @Valid @RequestBody DrawingCreateRequest req) {
        return elementService.createDrawing(req, boardId, getUserId());
    }

    @PatchMapping("/drawing/{id}")
    public DrawingElement update(@PathVariable String id, @Valid @RequestBody DrawingUpdateRequest req) {
        return elementService.updateDrawing(id, req, getUserId());
    }

    @DeleteMapping("/drawing/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        elementService.deleteElement(id, getUserId());
    }

    private Integer getUserId() {
        return userService.getCurrentUser().getId();
    }
}
