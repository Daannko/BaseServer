package dev.dankoz.BaseServer.app.board.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TiptapDocs {

    private TiptapDocs() {
    }

    public static Map<String, Object> emptyDoc() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("type", "doc");
        doc.put("content", new ArrayList<>());
        return doc;
    }

    public static Map<String, Object> textDoc(String text) {
        if (text == null) {
            return emptyDoc();
        }

        Map<String, Object> textNode = new HashMap<>();
        textNode.put("type", "text");
        textNode.put("text", text);

        Map<String, Object> paragraph = new HashMap<>();
        paragraph.put("type", "paragraph");
        paragraph.put("content", List.of(textNode));

        Map<String, Object> doc = new HashMap<>();
        doc.put("type", "doc");
        doc.put("content", List.of(paragraph));
        return doc;
    }
}
