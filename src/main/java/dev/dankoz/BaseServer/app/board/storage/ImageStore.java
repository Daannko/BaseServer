package dev.dankoz.BaseServer.app.board.storage;

import java.util.Optional;

/**
 * Abstraction over where board image bytes live. The current implementation writes to the local
 * filesystem; swapping to S3 (or compatible) later means a new implementation + config only — the
 * returned {@code key} stays storage-agnostic so existing {@code Topic.content} references keep working.
 */
public interface ImageStore {

    /** Store bytes for a board and return a stable key/url + detected dimensions. */
    StoredImage put(String boardId, byte[] bytes, String contentType);

    /** Read bytes back by key for serving, or empty if missing. */
    Optional<LoadedImage> load(String key);

    /** Delete the backing object; no-op if already gone. */
    void delete(String key);

    /**
     * Delete by a stored reference as it appears in {@code Topic.content} — accepts either a raw key
     * or an app-relative media url; no-op if it isn't one of ours or already gone.
     */
    void deleteByReference(String reference);
}
