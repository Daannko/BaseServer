package dev.dankoz.BaseServer.app.board.controller;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.dankoz.BaseServer.app.board.dto.ImageCreateRequest;
import dev.dankoz.BaseServer.app.board.dto.ImageUpdateRequest;
import dev.dankoz.BaseServer.app.board.dto.ImageUploadResponse;
import dev.dankoz.BaseServer.app.board.model.ImageElement;
import dev.dankoz.BaseServer.app.board.service.BoardService;
import dev.dankoz.BaseServer.app.board.service.ElementService;
import dev.dankoz.BaseServer.app.board.storage.ImageStore;
import dev.dankoz.BaseServer.app.board.storage.StoredImage;
import dev.dankoz.BaseServer.config.exceptions.BadRequestException;
import dev.dankoz.BaseServer.general.service.UserService;
import jakarta.validation.Valid;

@RestController
public class ImageController {

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp", "image/gif");

    private final BoardService boardService;
    private final UserService userService;
    private final ImageStore imageStore;
    private final ElementService elementService;
    private final long maxBytes;

    public ImageController(BoardService boardService, UserService userService, ImageStore imageStore,
            ElementService elementService,
            @Value("${app.storage.image.max-bytes:10485760}") long maxBytes) {
        this.boardService = boardService;
        this.userService = userService;
        this.imageStore = imageStore;
        this.elementService = elementService;
        this.maxBytes = maxBytes;
    }

    @PostMapping("/board/{boardId}/image/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ImageUploadResponse upload(@PathVariable String boardId, @RequestParam("file") MultipartFile file) {
        boardService.getBoard(boardId, getUserId());

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("file is required");
        }
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("file exceeds " + maxBytes + " bytes");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported image type: " + contentType);
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Failed to read upload: " + e.getMessage());
        }

        StoredImage stored = imageStore.put(boardId, bytes, contentType);
        return new ImageUploadResponse(stored.url(), stored.width(), stored.height());
    }

    @PostMapping("/board/{boardId}/image")
    @ResponseStatus(HttpStatus.CREATED)
    public ImageElement create(@PathVariable String boardId, @Valid @RequestBody ImageCreateRequest req) {
        return elementService.createImage(req, boardId, getUserId());
    }

    @PatchMapping("/image/{id}")
    public ImageElement update(@PathVariable String id, @Valid @RequestBody ImageUpdateRequest req) {
        return elementService.updateImage(id, req, getUserId());
    }

    @DeleteMapping("/image/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        elementService.deleteElement(id, getUserId());
    }

    private Integer getUserId() {
        return userService.getCurrentUser().getId();
    }
}
