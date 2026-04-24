package com.library.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LibraryException extends RuntimeException {

    public enum Severity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    public enum Category {
        VALIDATION,
        NOT_FOUND,
        CONFLICT,
        BUSINESS_RULE,
        PERMISSION,
        INTERNAL,
        EXTERNAL
    }

    private final String errorCode;
    private final Severity severity;
    private final Category category;
    private final Instant occurredAt;
    private final Map<String, Object> context;

    public LibraryException(String message) {
        this(message, null, "LIB_ERR", Severity.ERROR, Category.INTERNAL);
    }

    public LibraryException(String message, Throwable cause) {
        this(message, cause, "LIB_ERR", Severity.ERROR, Category.INTERNAL);
    }

    public LibraryException(String message, String errorCode) {
        this(message, null, errorCode, Severity.ERROR, Category.INTERNAL);
    }

    public LibraryException(String message, String errorCode, Category category) {
        this(message, null, errorCode, Severity.ERROR, category);
    }

    public LibraryException(String message,
                             Throwable cause,
                             String errorCode,
                             Severity severity,
                             Category category) {
        super(message, cause);
        this.errorCode = errorCode == null ? "LIB_ERR" : errorCode;
        this.severity = severity == null ? Severity.ERROR : severity;
        this.category = category == null ? Category.INTERNAL : category;
        this.occurredAt = Instant.now();
        this.context = new HashMap<>();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Category getCategory() {
        return category;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }

    public LibraryException withContext(String key, Object value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Context key cannot be null or blank");
        }
        this.context.put(key, value);
        return this;
    }

    public boolean isRetryable() {
        return category == Category.EXTERNAL
                || (severity == Severity.WARNING && category != Category.VALIDATION);
    }

    public boolean isClientError() {
        return category == Category.VALIDATION
                || category == Category.NOT_FOUND
                || category == Category.CONFLICT
                || category == Category.PERMISSION
                || category == Category.BUSINESS_RULE;
    }

    public boolean isServerError() {
        return category == Category.INTERNAL || category == Category.EXTERNAL;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"errorCode\":\"").append(escape(errorCode)).append("\",");
        sb.append("\"message\":\"").append(escape(getMessage())).append("\",");
        sb.append("\"severity\":\"").append(severity.name()).append("\",");
        sb.append("\"category\":\"").append(category.name()).append("\",");
        sb.append("\"occurredAt\":\"").append(occurredAt.toString()).append("\"");
        if (!context.isEmpty()) {
            sb.append(",\"context\":{");
            boolean first = true;
            for (Map.Entry<String, Object> e : context.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escape(e.getKey())).append("\":\"")
                        .append(escape(String.valueOf(e.getValue()))).append("\"");
                first = false;
            }
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + errorCode + ":" + category + "/" + severity + "] "
                + getMessage();
    }

    public static LibraryException notFound(String resource, String id) {
        return new LibraryException(
                resource + " not found: " + id,
                null,
                "LIB_NOT_FOUND",
                Severity.WARNING,
                Category.NOT_FOUND
        ).withContext("resource", resource).withContext("id", id);
    }

    public static LibraryException conflict(String message) {
        return new LibraryException(message, null, "LIB_CONFLICT", Severity.WARNING, Category.CONFLICT);
    }

    public static LibraryException validation(String message) {
        return new LibraryException(message, null, "LIB_VALIDATION", Severity.WARNING, Category.VALIDATION);
    }

    public static LibraryException businessRule(String message) {
        return new LibraryException(message, null, "LIB_BUSINESS", Severity.WARNING, Category.BUSINESS_RULE);
    }

    public static LibraryException permission(String message) {
        return new LibraryException(message, null, "LIB_PERMISSION", Severity.ERROR, Category.PERMISSION);
    }

    public static LibraryException internal(String message, Throwable cause) {
        return new LibraryException(message, cause, "LIB_INTERNAL", Severity.CRITICAL, Category.INTERNAL);
    }

    public static LibraryException external(String service, String message) {
        return new LibraryException(
                "External service failure [" + service + "]: " + message,
                null,
                "LIB_EXTERNAL",
                Severity.ERROR,
                Category.EXTERNAL
        ).withContext("service", service);
    }
}
