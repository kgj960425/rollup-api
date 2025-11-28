package com.rollup.rollupapi.games.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 액션 검증 결과
 */
@Getter
@AllArgsConstructor
public class ValidationResult {
    private final boolean valid;
    private final String message;

    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(String message) {
        return new ValidationResult(false, message);
    }
}
