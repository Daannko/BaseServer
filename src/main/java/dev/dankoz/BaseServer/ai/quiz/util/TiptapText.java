package dev.dankoz.BaseServer.ai.quiz.util;

import java.util.List;
import java.util.Map;

/** Extracts plain text from a TiptapDoc ({@code Map<String,Object>}) for LLM prompts. */
public final class TiptapText {

    private TiptapText() {
    }

    public static String toPlainText(Map<String, Object> doc) {
        if (doc == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        walk(doc, sb);
        return sb.toString().strip();
    }

    @SuppressWarnings("unchecked")
    private static void walk(Object node, StringBuilder sb) {
        if (node instanceof Map<?, ?> map) {
            Object type = map.get("type");
            Object text = map.get("text");
            if (text instanceof String s) {
                sb.append(s);
            }
            Object content = map.get("content");
            if (content instanceof List<?> list) {
                for (Object child : list) {
                    walk(child, sb);
                }
            }
            // Block-level nodes get a newline so paragraphs/headings/list items separate.
            if (type instanceof String t && isBlock(t)) {
                sb.append('\n');
            }
        } else if (node instanceof List<?> list) {
            for (Object child : list) {
                walk(child, sb);
            }
        }
    }

    private static boolean isBlock(String type) {
        return switch (type) {
            case "paragraph", "heading", "listItem", "bulletList", "orderedList",
                 "blockquote", "codeBlock", "hardBreak" -> true;
            default -> false;
        };
    }
}
