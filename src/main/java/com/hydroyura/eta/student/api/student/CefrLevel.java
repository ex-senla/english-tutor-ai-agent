package com.hydroyura.eta.student.api.student;

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
}
