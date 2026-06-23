package dev.dankoz.BaseServer.app.board.storage;

/** Image bytes plus their content type, as read back from the store for serving. */
public record LoadedImage(byte[] data, String contentType) {}
