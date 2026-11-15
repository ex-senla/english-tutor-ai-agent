package com.hydroyura.eta.chatbot.view.util;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViewUtilsTest {

    @Test
    void shouldReturnEmptyForNullLabel() {
        assertThat(ViewUtils.truncateButtonLabel(null)).isEmpty();
    }

    @Test
    void shouldKeepShortLabelUnchanged() {
        assertThat(ViewUtils.truncateButtonLabel("apple")).isEqualTo("apple");
    }

    @Test
    void shouldKeepExactly64ByteLabelUnchanged() {
        var label = "a".repeat(64);
        assertThat(ViewUtils.truncateButtonLabel(label)).isEqualTo(label);
    }

    @Test
    void shouldTruncateLongAsciiLabelWithEllipsis() {
        var label = "a".repeat(65);
        var result = ViewUtils.truncateButtonLabel(label);

        assertThat(result).isEqualTo("a".repeat(61) + "…");
        assertThat(result.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
    }

    @Test
    void shouldNotSplitMultibyteCyrillicSymbols() {
        var label = "э".repeat(50);
        var result = ViewUtils.truncateButtonLabel(label);

        assertThat(result).isEqualTo("э".repeat(30) + "…");
        assertThat(result).doesNotContain("\uFFFD");
        assertThat(result.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
    }

    @Test
    void shouldNotSplitEmojiAtBoundary() {
        var label = "a".repeat(61) + "😀";
        var result = ViewUtils.truncateButtonLabel(label);

        assertThat(result).isEqualTo("a".repeat(61) + "…");
        assertThat(result).doesNotContain("\uFFFD");
    }

    @Test
    void shouldComputePageCount() {
        assertThat(ViewUtils.pageCount(0, 8)).isZero();
        assertThat(ViewUtils.pageCount(8, 8)).isEqualTo(1);
        assertThat(ViewUtils.pageCount(9, 8)).isEqualTo(2);
        assertThat(ViewUtils.pageCount(20, 8)).isEqualTo(3);
        assertThat(ViewUtils.pageCount(9, 0)).isEqualTo(9);
    }

    @Test
    void shouldComputePaginationBoundaries() {
        assertThat(ViewUtils.hasPrev(0)).isFalse();
        assertThat(ViewUtils.hasPrev(1)).isTrue();
        assertThat(ViewUtils.hasNext(0, 1)).isFalse();
        assertThat(ViewUtils.hasNext(0, 2)).isTrue();
        assertThat(ViewUtils.hasNext(1, 2)).isFalse();
    }

    @Test
    void shouldSlicePages() {
        var items = IntStream.rangeClosed(1, 10).boxed().toList();

        assertThat(ViewUtils.pageSlice(items, 0, 8)).containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
        assertThat(ViewUtils.pageSlice(items, 1, 8)).containsExactly(9, 10);
        assertThat(ViewUtils.pageSlice(items, 2, 8)).isEmpty();
        assertThat(ViewUtils.pageSlice(items, -1, 8)).containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
    }
}
