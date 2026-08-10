package com.contactmanager.exception;

import java.util.List;

public class ExcelImportException extends RuntimeException {

    private final List<String> errors;

    public ExcelImportException(String message) {
        this(message, List.of());
    }

    public ExcelImportException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
