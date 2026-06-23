package dev.dankoz.BaseServer.app.board.storage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.dankoz.BaseServer.config.exceptions.BadRequestException;

/**
 * Filesystem-backed {@link ImageStore}. Bytes land under {@code <base-path>/boards/{boardId}/{uuid}.{ext}}
 * and are referenced by the relative key {@code boards/{boardId}/{uuid}.{ext}}.
 */
@Service
public class LocalImageStore implements ImageStore {

    private final Path basePath;
    private final String mediaBaseUrl;

    public LocalImageStore(
            @Value("${app.storage.local.base-path:./uploads}") String basePath,
            @Value("${app.media.base-url:/media}") String mediaBaseUrl) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        this.mediaBaseUrl = mediaBaseUrl.endsWith("/")
                ? mediaBaseUrl.substring(0, mediaBaseUrl.length() - 1)
                : mediaBaseUrl;
    }

    @Override
    public StoredImage put(String boardId, byte[] bytes, String contentType) {
        String ext = extensionFor(contentType);
        String key = "boards/" + boardId + "/" + UUID.randomUUID() + "." + ext;
        Path target = resolve(key);

        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store image: " + e.getMessage());
        }

        Integer width = null;
        Integer height = null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            BufferedImage img = ImageIO.read(in);
            if (img != null) {
                width = img.getWidth();
                height = img.getHeight();
            }
        } catch (IOException ignored) {
            // dimensions are best-effort (e.g. no ImageIO reader for webp) -> leave null
        }

        return new StoredImage(key, mediaBaseUrl + "/" + key, width, height, bytes.length);
    }

    @Override
    public Optional<LoadedImage> load(String key) {
        Path target = resolve(key);
        if (!Files.exists(target)) {
            return Optional.empty();
        }
        try {
            byte[] data = Files.readAllBytes(target);
            return Optional.of(new LoadedImage(data, contentTypeFor(key)));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ignored) {
            // best-effort delete
        }
    }

    @Override
    public void deleteByReference(String reference) {
        if (reference == null || reference.isBlank()) {
            return;
        }
        String key = reference;
        String prefix = mediaBaseUrl + "/";
        if (key.startsWith(prefix)) {
            key = key.substring(prefix.length());
        }
        if (key.contains("..") || !key.startsWith("boards/")) {
            return; // not one of ours
        }
        delete(key);
    }

    /** Resolve a key under the base path, rejecting traversal outside it. */
    private Path resolve(String key) {
        if (key == null || key.isBlank() || key.contains("..")) {
            throw new BadRequestException("Invalid image key");
        }
        Path resolved = basePath.resolve(key).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new BadRequestException("Invalid image key");
        }
        return resolved;
    }

    private static String extensionFor(String contentType) {
        if (contentType == null) {
            throw new BadRequestException("Missing content type");
        }
        switch (contentType.toLowerCase()) {
            case "image/png":
                return "png";
            case "image/jpeg":
                return "jpg";
            case "image/webp":
                return "webp";
            case "image/gif":
                return "gif";
            default:
                throw new BadRequestException("Unsupported image type: " + contentType);
        }
    }

    private static String contentTypeFor(String key) {
        String lower = key.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }
}
