package com.library.util;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9 .()-]{7,20}$");
    private static final Pattern ISBN10_PATTERN =
            Pattern.compile("^[0-9]{9}[0-9Xx]$");
    private static final Pattern ISBN13_PATTERN =
            Pattern.compile("^[0-9]{13}$");
    private static final Pattern URL_PATTERN =
            Pattern.compile("^https?://[\\w.-]+(:[0-9]+)?(/.*)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEX_COLOR_PATTERN =
            Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final Pattern POSTAL_CODE_PATTERN =
            Pattern.compile("^[A-Za-z0-9 -]{3,10}$");

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
        return value;
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    public static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
    }

    public static double requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    public static double requireNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
    }

    public static int requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max + ", got " + value);
        }
        return value;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static String requireEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        return email;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static String requirePhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone: " + phone);
        }
        return phone;
    }

    public static boolean isValidIsbn10(String isbn) {
        if (isbn == null) return false;
        String cleaned = isbn.replaceAll("-", "");
        if (!ISBN10_PATTERN.matcher(cleaned).matches()) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cleaned.charAt(i) - '0') * (10 - i);
        }
        char last = cleaned.charAt(9);
        sum += (last == 'X' || last == 'x') ? 10 : (last - '0');
        return sum % 11 == 0;
    }

    public static boolean isValidIsbn13(String isbn) {
        if (isbn == null) return false;
        String cleaned = isbn.replaceAll("-", "");
        if (!ISBN13_PATTERN.matcher(cleaned).matches()) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            int digit = cleaned.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return sum % 10 == 0;
    }

    public static boolean isValidIsbn(String isbn) {
        return isValidIsbn10(isbn) || isValidIsbn13(isbn);
    }

    public static String requireIsbn(String isbn) {
        if (!isValidIsbn(isbn)) {
            throw new IllegalArgumentException("Invalid ISBN: " + isbn);
        }
        return isbn;
    }

    public static boolean isValidUrl(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }

    public static boolean isValidHexColor(String hex) {
        return hex != null && HEX_COLOR_PATTERN.matcher(hex).matches();
    }

    public static boolean isValidPostalCode(String code) {
        return code != null && POSTAL_CODE_PATTERN.matcher(code).matches();
    }

    public static String requireMaxLength(String value, int maxLength, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " cannot exceed " + maxLength + " characters");
        }
        return value;
    }

    public static String requireMinLength(String value, int minLength, String fieldName) {
        if (value == null || value.length() < minLength) {
            throw new IllegalArgumentException(
                    fieldName + " must be at least " + minLength + " characters");
        }
        return value;
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    public static boolean isAlphanumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isLetterOrDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasSpecialChar(String value) {
        if (value == null) return false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
}
