package com.hydroyura.eta.teacher.api.teacher;

import lombok.Getter;

@Getter
public enum IdentifierType {
    TELEGRAM(Long.class);

    private final Class<?> valueClass;

    IdentifierType(Class<?> valueClass) {
        this.valueClass = valueClass;
    }

}
