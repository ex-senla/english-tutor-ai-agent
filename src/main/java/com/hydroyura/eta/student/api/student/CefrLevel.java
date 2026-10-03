package com.hydroyura.eta.student.api.student;

import java.util.Optional;

public enum CefrLevel {

    A1("A1"),
    A2("A2"),
    B1("B1"),
    B2("B2"),
    C1("C1"),
    C2("C2");

    private final String code;

    CefrLevel(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Optional<CefrLevel> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(CefrLevel.valueOf(code));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
