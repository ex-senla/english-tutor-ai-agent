package com.hydroyura.eta.student.api.student;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CefrLevelTest {

    @Test
    void shouldExposeCode() {
        assertThat(CefrLevel.A1.code()).isEqualTo("A1");
        assertThat(CefrLevel.A2.code()).isEqualTo("A2");
        assertThat(CefrLevel.B1.code()).isEqualTo("B1");
        assertThat(CefrLevel.B2.code()).isEqualTo("B2");
        assertThat(CefrLevel.C1.code()).isEqualTo("C1");
        assertThat(CefrLevel.C2.code()).isEqualTo("C2");
    }

    @Test
    void shouldParseCode() {
        assertThat(CefrLevel.fromCode("A2")).contains(CefrLevel.A2);
        assertThat(CefrLevel.fromCode("C2")).contains(CefrLevel.C2);
    }

    @Test
    void shouldReturnEmptyForInvalidCode() {
        assertThat(CefrLevel.fromCode("ZZ")).isEmpty();
        assertThat(CefrLevel.fromCode(null)).isEmpty();
        assertThat(CefrLevel.fromCode("  ")).isEmpty();
    }
}
