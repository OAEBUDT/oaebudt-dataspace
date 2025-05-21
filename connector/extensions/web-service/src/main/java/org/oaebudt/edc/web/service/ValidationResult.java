package org.oaebudt.edc.web.service;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public void addError(String message) {
        errors.add(message);
    }

    public void merge(ValidationResult other) {
        errors.addAll(other.getErrors());
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public boolean failed() {
        return !isValid();
    }

    public List<String> getErrors() {
        return errors;
    }

    public static ValidationResult ok() {
        return new ValidationResult();
    }

    public static ValidationResult fail(String message) {
        ValidationResult result = new ValidationResult();
        result.addError(message);
        return result;
    }
}
