package com.hydroyura.eta.chatbot.view.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewUtils {

    private static final int TELEGRAM_BUTTON_LABEL_MAX_BYTES = 64;

    private static final String ELLIPSIS = "…";

    public static String createCallbackData(String... data) {
        return String.join(":", data);
    }

    /**
     * Обрезает подпись inline-кнопки до 64 байт UTF-8 (лимит Telegram на текст кнопки),
     * не разрывая многобайтовые символы, и добавляет «…» при обрезке.
     */
    public static String truncateButtonLabel(String label) {
        if (label == null) {
            return "";
        }
        var bytes = label.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= TELEGRAM_BUTTON_LABEL_MAX_BYTES) {
            return label;
        }

        var budget = TELEGRAM_BUTTON_LABEL_MAX_BYTES - ELLIPSIS.getBytes(StandardCharsets.UTF_8).length;
        var result = new StringBuilder();
        var used = 0;
        for (var i = 0; i < label.length();) {
            var codePoint = label.codePointAt(i);
            var symbol = new String(Character.toChars(codePoint));
            var symbolBytes = symbol.getBytes(StandardCharsets.UTF_8).length;
            if (used + symbolBytes > budget) {
                break;
            }
            result.append(symbol);
            used += symbolBytes;
            i += Character.charCount(codePoint);
        }
        return result.append(ELLIPSIS).toString();
    }

    public static int pageCount(int totalItems, int pageSize) {
        if (totalItems <= 0) {
            return 0;
        }
        var safePageSize = Math.max(1, pageSize);
        return (totalItems + safePageSize - 1) / safePageSize;
    }

    public static boolean hasPrev(int page) {
        return page > 0;
    }

    public static boolean hasNext(int page, int pageCount) {
        return page >= 0 && page + 1 < pageCount;
    }

    public static <T> List<T> pageSlice(List<T> items, int page, int pageSize) {
        var safePageSize = Math.max(1, pageSize);
        var safePage = Math.max(0, page);
        var from = safePage * safePageSize;
        if (from >= items.size()) {
            return List.of();
        }
        var to = Math.min(items.size(), from + safePageSize);
        return new ArrayList<>(items.subList(from, to));
    }
}
