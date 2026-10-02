package com.hydroyura.eta.generator.api.document;

import com.hydroyura.eta.shared.api.DomainException;

public class PdfRenderException extends DomainException {

    public PdfRenderException(String message) {
        super(message);
    }

    public PdfRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
