package com.library.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Loan {

    public enum Status {
        ACTIVE,
        RETURNED,
        OVERDUE,
        LOST,
        RENEWED
    }

    private static final double DAILY_LATE_FEE = 0.50;
    private static final double LOST_BOOK_MULTIPLIER = 1.25;
    private static final int MAX_RENEWALS = 2;

    private final String id;
    private final String bookId;
    private final String memberId;
    private final LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Status status;
    private int renewalCount;
    private double fineAmount;
    private String notes;
    private LocalDate lastStatusChange;

    public Loan(String id, String bookId, String memberId, LocalDate borrowDate, LocalDate dueDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Loan id cannot be null or blank");
        }
        if (bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Book id cannot be null or blank");
        }
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member id cannot be null or blank");
        }
        if (borrowDate == null || dueDate == null) {
            throw new IllegalArgumentException("Borrow and due dates must be provided");
        }
        if (dueDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Due date cannot be before borrow date");
        }
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = Status.ACTIVE;
        this.renewalCount = 0;
        this.fineAmount = 0.0;
        this.lastStatusChange = borrowDate;
    }

    public String getId() {
        return id;
    }

    public String getBookId() {
        return bookId;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public Status getStatus() {
        return status;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getLastStatusChange() {
        return lastStatusChange;
    }

    public boolean isActive() {
        return status == Status.ACTIVE || status == Status.RENEWED || status == Status.OVERDUE;
    }

    public boolean isOverdue() {
        return isOverdueOn(LocalDate.now());
    }

    public boolean isOverdueOn(LocalDate date) {
        if (!isActive()) {
            return false;
        }
        return date.isAfter(dueDate);
    }

    public long daysOverdueOn(LocalDate date) {
        if (!isOverdueOn(date)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dueDate, date);
    }

    public long daysUntilDue() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    public boolean renew(int additionalDays) {
        if (renewalCount >= MAX_RENEWALS) {
            return false;
        }
        if (!isActive()) {
            return false;
        }
        if (isOverdue()) {
            return false;
        }
        if (additionalDays <= 0) {
            throw new IllegalArgumentException("Additional days must be positive");
        }
        this.dueDate = dueDate.plusDays(additionalDays);
        this.renewalCount += 1;
        this.status = Status.RENEWED;
        this.lastStatusChange = LocalDate.now();
        return true;
    }

    public double returnOn(LocalDate date, double bookPrice) {
        if (!isActive()) {
            throw new IllegalStateException("Cannot return a loan with status " + status);
        }
        if (date == null) {
            throw new IllegalArgumentException("Return date cannot be null");
        }
        if (date.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Return date cannot be before borrow date");
        }
        this.returnDate = date;
        long overdueDays = daysOverdueOn(date);
        if (overdueDays > 0) {
            this.fineAmount = overdueDays * DAILY_LATE_FEE;
        }
        this.status = Status.RETURNED;
        this.lastStatusChange = date;
        return fineAmount;
    }

    public double markLost(double bookPrice) {
        if (bookPrice < 0) {
            throw new IllegalArgumentException("Book price cannot be negative");
        }
        this.fineAmount = bookPrice * LOST_BOOK_MULTIPLIER;
        this.status = Status.LOST;
        this.lastStatusChange = LocalDate.now();
        return fineAmount;
    }

    public void refreshOverdueStatus() {
        if (status == Status.ACTIVE || status == Status.RENEWED) {
            if (isOverdue()) {
                status = Status.OVERDUE;
                lastStatusChange = LocalDate.now();
            }
        }
    }

    public long loanDurationDays() {
        LocalDate end = returnDate != null ? returnDate : LocalDate.now();
        return ChronoUnit.DAYS.between(borrowDate, end);
    }

    public boolean canRenew() {
        return isActive() && !isOverdue() && renewalCount < MAX_RENEWALS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan)) return false;
        return Objects.equals(id, ((Loan) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Loan{id='" + id + "', book='" + bookId + "', member='" + memberId +
                "', due=" + dueDate + ", status=" + status + ", fine=" + fineAmount + "}";
    }
}
