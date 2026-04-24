package com.library.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Member {

    public enum MembershipType {
        STANDARD(3, 14),
        STUDENT(5, 21),
        PREMIUM(10, 30),
        STAFF(15, 60),
        CHILD(2, 14);

        private final int maxActiveLoans;
        private final int loanDurationDays;

        MembershipType(int maxActiveLoans, int loanDurationDays) {
            this.maxActiveLoans = maxActiveLoans;
            this.loanDurationDays = loanDurationDays;
        }

        public int getMaxActiveLoans() {
            return maxActiveLoans;
        }

        public int getLoanDurationDays() {
            return loanDurationDays;
        }
    }

    public enum Status {
        ACTIVE,
        SUSPENDED,
        EXPIRED,
        CLOSED
    }

    private final String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate joinDate;
    private LocalDate membershipExpiry;
    private MembershipType membershipType;
    private Status status;
    private double outstandingFine;
    private final List<String> activeLoanIds;
    private final List<String> loanHistoryIds;
    private int totalBooksRead;

    public Member(String id, String firstName, String lastName, String email) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Member id cannot be null or blank");
        }
        this.id = id;
        this.firstName = requireNonBlank(firstName, "firstName");
        this.lastName = requireNonBlank(lastName, "lastName");
        this.email = requireNonBlank(email, "email");
        this.joinDate = LocalDate.now();
        this.membershipExpiry = joinDate.plusYears(1);
        this.membershipType = MembershipType.STANDARD;
        this.status = Status.ACTIVE;
        this.outstandingFine = 0.0;
        this.activeLoanIds = new ArrayList<>();
        this.loanHistoryIds = new ArrayList<>();
        this.totalBooksRead = 0;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
        return value;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = requireNonBlank(firstName, "firstName");
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = requireNonBlank(lastName, "lastName");
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        this.dateOfBirth = dateOfBirth;
    }

    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public LocalDate getMembershipExpiry() {
        return membershipExpiry;
    }

    public void extendMembership(int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Extension months must be positive");
        }
        LocalDate base = membershipExpiry.isBefore(LocalDate.now()) ? LocalDate.now() : membershipExpiry;
        this.membershipExpiry = base.plusMonths(months);
        if (this.status == Status.EXPIRED) {
            this.status = Status.ACTIVE;
        }
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType == null ? MembershipType.STANDARD : membershipType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status == null ? Status.ACTIVE : status;
    }

    public double getOutstandingFine() {
        return outstandingFine;
    }

    public void addFine(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Fine amount cannot be negative");
        }
        this.outstandingFine += amount;
    }

    public void payFine(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        if (amount > outstandingFine) {
            throw new IllegalArgumentException("Payment exceeds outstanding fine");
        }
        this.outstandingFine -= amount;
    }

    public List<String> getActiveLoanIds() {
        return Collections.unmodifiableList(activeLoanIds);
    }

    public void addActiveLoan(String loanId) {
        if (activeLoanIds.size() >= membershipType.getMaxActiveLoans()) {
            throw new IllegalStateException("Active loan limit reached for " + membershipType);
        }
        activeLoanIds.add(loanId);
    }

    public void completeLoan(String loanId) {
        if (activeLoanIds.remove(loanId)) {
            loanHistoryIds.add(loanId);
            totalBooksRead += 1;
        }
    }

    public List<String> getLoanHistoryIds() {
        return Collections.unmodifiableList(loanHistoryIds);
    }

    public int getTotalBooksRead() {
        return totalBooksRead;
    }

    public boolean canBorrow() {
        return status == Status.ACTIVE
                && membershipExpiry.isAfter(LocalDate.now())
                && activeLoanIds.size() < membershipType.getMaxActiveLoans()
                && outstandingFine < 50.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        return Objects.equals(id, ((Member) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Member{id='" + id + "', name='" + getFullName() + "', type=" + membershipType +
                ", status=" + status + ", activeLoans=" + activeLoanIds.size() + "}";
    }
}
