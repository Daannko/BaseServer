package dev.dankoz.BaseServer.app.board.storage;

/** Result of storing image bytes: a storage-agnostic key plus an app-relative url and metadata. */
public record StoredImage(
    String key,
    String url,
    Integer width,
    Integer height,
    long bytes
) {}
