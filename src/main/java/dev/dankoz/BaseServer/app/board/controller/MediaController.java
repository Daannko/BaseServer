package dev.dankoz.BaseServer.app.board.controller;

import java.util.Optional;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import dev.dankoz.BaseServer.app.board.storage.ImageStore;
import dev.dankoz.BaseServer.app.board.storage.LoadedImage;

import java.time.Duration;

@RestController
public class MediaController {

    private final ImageStore imageStore;

    public MediaController(ImageStore imageStore) {
        this.imageStore = imageStore;
    }

    @GetMapping("/media/{*key}")
    public ResponseEntity<byte[]> serve(@PathVariable String key) {
        // {*key} captures with a leading slash; strip it to get the storage key.
        String normalized = key.startsWith("/") ? key.substring(1) : key;

        Optional<LoadedImage> loaded = imageStore.load(normalized);
        if (loaded.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LoadedImage image = loaded.get();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
                .body(image.data());
    }
}
