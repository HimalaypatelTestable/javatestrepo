package com.library.util;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {

    private static final String BOOK_PREFIX = "BK";
    private static final String MEMBER_PREFIX = "MB";
    private static final String LOAN_PREFIX = "LN";
    private static final String AUTHOR_PREFIX = "AU";
    private static final String PUBLISHER_PREFIX = "PB";
    private static final String CATEGORY_PREFIX = "CT";
    private static final String RESERVATION_PREFIX = "RS";
    private static final String FINE_PREFIX = "FN";

    private final AtomicLong bookSequence;
    private final AtomicLong memberSequence;
    private final AtomicLong loanSequence;
    private final AtomicLong authorSequence;
    private final AtomicLong publisherSequence;
    private final AtomicLong categorySequence;
    private final AtomicLong reservationSequence;
    private final AtomicLong fineSequence;
    private final int padWidth;

    public IdGenerator() {
        this(6);
    }

    public IdGenerator(int padWidth) {
        if (padWidth < 1 || padWidth > 12) {
            throw new IllegalArgumentException("Pad width must be between 1 and 12");
        }
        this.padWidth = padWidth;
        this.bookSequence = new AtomicLong(0);
        this.memberSequence = new AtomicLong(0);
        this.loanSequence = new AtomicLong(0);
        this.authorSequence = new AtomicLong(0);
        this.publisherSequence = new AtomicLong(0);
        this.categorySequence = new AtomicLong(0);
        this.reservationSequence = new AtomicLong(0);
        this.fineSequence = new AtomicLong(0);
    }

    public String nextBookId() {
        return format(BOOK_PREFIX, bookSequence.incrementAndGet());
    }

    public String nextMemberId() {
        return format(MEMBER_PREFIX, memberSequence.incrementAndGet());
    }

    public String nextLoanId() {
        return format(LOAN_PREFIX, loanSequence.incrementAndGet());
    }

    public String nextAuthorId() {
        return format(AUTHOR_PREFIX, authorSequence.incrementAndGet());
    }

    public String nextPublisherId() {
        return format(PUBLISHER_PREFIX, publisherSequence.incrementAndGet());
    }

    public String nextCategoryId() {
        return format(CATEGORY_PREFIX, categorySequence.incrementAndGet());
    }

    public String nextReservationId() {
        return format(RESERVATION_PREFIX, reservationSequence.incrementAndGet());
    }

    public String nextFineId() {
        return format(FINE_PREFIX, fineSequence.incrementAndGet());
    }

    private String format(String prefix, long value) {
        return prefix + "-" + pad(value);
    }

    private String pad(long value) {
        String raw = Long.toString(value);
        if (raw.length() >= padWidth) {
            return raw;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padWidth - raw.length(); i++) {
            sb.append('0');
        }
        sb.append(raw);
        return sb.toString();
    }

    public String randomUuid() {
        return UUID.randomUUID().toString();
    }

    public String dateStampedId(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Prefix cannot be null or blank");
        }
        return prefix.toUpperCase() + "-" + LocalDate.now().toString().replace("-", "") + "-" + pad(nextSequenceFor(prefix));
    }

    private long nextSequenceFor(String prefix) {
        switch (prefix.toUpperCase()) {
            case "BK": return bookSequence.incrementAndGet();
            case "MB": return memberSequence.incrementAndGet();
            case "LN": return loanSequence.incrementAndGet();
            case "AU": return authorSequence.incrementAndGet();
            case "PB": return publisherSequence.incrementAndGet();
            case "CT": return categorySequence.incrementAndGet();
            case "RS": return reservationSequence.incrementAndGet();
            case "FN": return fineSequence.incrementAndGet();
            default: return System.nanoTime();
        }
    }

    public synchronized void reset() {
        bookSequence.set(0);
        memberSequence.set(0);
        loanSequence.set(0);
        authorSequence.set(0);
        publisherSequence.set(0);
        categorySequence.set(0);
        reservationSequence.set(0);
        fineSequence.set(0);
    }

    public long getBookCount() {
        return bookSequence.get();
    }

    public long getMemberCount() {
        return memberSequence.get();
    }

    public long getLoanCount() {
        return loanSequence.get();
    }

    public long getAuthorCount() {
        return authorSequence.get();
    }

    public long getPublisherCount() {
        return publisherSequence.get();
    }

    public long getCategoryCount() {
        return categorySequence.get();
    }

    public long getReservationCount() {
        return reservationSequence.get();
    }

    public long getFineCount() {
        return fineSequence.get();
    }

    public long totalIdsIssued() {
        return getBookCount() + getMemberCount() + getLoanCount()
                + getAuthorCount() + getPublisherCount() + getCategoryCount()
                + getReservationCount() + getFineCount();
    }

    public static boolean isValidId(String id, String expectedPrefix) {
        if (id == null || expectedPrefix == null) {
            return false;
        }
        String expected = expectedPrefix + "-";
        if (!id.startsWith(expected)) {
            return false;
        }
        String suffix = id.substring(expected.length());
        if (suffix.isEmpty()) {
            return false;
        }
        for (int i = 0; i < suffix.length(); i++) {
            if (!Character.isDigit(suffix.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String extractPrefix(String id) {
        if (id == null) {
            return null;
        }
        int dash = id.indexOf('-');
        return dash > 0 ? id.substring(0, dash) : null;
    }

    public static long extractSequence(String id) {
        if (id == null) {
            return -1;
        }
        int dash = id.indexOf('-');
        if (dash < 0) {
            return -1;
        }
        try {
            return Long.parseLong(id.substring(dash + 1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
