package com.library.repository;

import com.library.model.Loan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LoanRepository {

    private final Map<String, Loan> loansById;
    private final Map<String, List<String>> loansByMember;
    private final Map<String, List<String>> loansByBook;

    public LoanRepository() {
        this.loansById = new ConcurrentHashMap<>();
        this.loansByMember = new HashMap<>();
        this.loansByBook = new HashMap<>();
    }

    public synchronized Loan save(Loan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("Loan cannot be null");
        }
        loansById.put(loan.getId(), loan);
        loansByMember.computeIfAbsent(loan.getMemberId(), k -> new ArrayList<>());
        if (!loansByMember.get(loan.getMemberId()).contains(loan.getId())) {
            loansByMember.get(loan.getMemberId()).add(loan.getId());
        }
        loansByBook.computeIfAbsent(loan.getBookId(), k -> new ArrayList<>());
        if (!loansByBook.get(loan.getBookId()).contains(loan.getId())) {
            loansByBook.get(loan.getBookId()).add(loan.getId());
        }
        return loan;
    }

    public Optional<Loan> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(loansById.get(id));
    }

    public List<Loan> findAll() {
        return new ArrayList<>(loansById.values());
    }

    public List<Loan> findByMemberId(String memberId) {
        if (memberId == null) {
            return Collections.emptyList();
        }
        List<String> ids = loansByMember.getOrDefault(memberId, Collections.emptyList());
        return ids.stream().map(loansById::get).filter(l -> l != null).collect(Collectors.toList());
    }

    public List<Loan> findActiveByMemberId(String memberId) {
        return findByMemberId(memberId).stream()
                .filter(Loan::isActive)
                .collect(Collectors.toList());
    }

    public List<Loan> findByBookId(String bookId) {
        if (bookId == null) {
            return Collections.emptyList();
        }
        List<String> ids = loansByBook.getOrDefault(bookId, Collections.emptyList());
        return ids.stream().map(loansById::get).filter(l -> l != null).collect(Collectors.toList());
    }

    public List<Loan> findActive() {
        return loansById.values().stream()
                .filter(Loan::isActive)
                .collect(Collectors.toList());
    }

    public List<Loan> findOverdue() {
        LocalDate today = LocalDate.now();
        return loansById.values().stream()
                .filter(l -> l.isOverdueOn(today))
                .sorted(Comparator.comparing(Loan::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Loan> findDueWithinDays(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Days cannot be negative");
        }
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(days);
        return loansById.values().stream()
                .filter(Loan::isActive)
                .filter(l -> !l.getDueDate().isBefore(today) && !l.getDueDate().isAfter(cutoff))
                .sorted(Comparator.comparing(Loan::getDueDate))
                .collect(Collectors.toList());
    }

    public List<Loan> findByStatus(Loan.Status status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return loansById.values().stream()
                .filter(l -> l.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Loan> findBorrowedBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date range");
        }
        return loansById.values().stream()
                .filter(l -> !l.getBorrowDate().isBefore(from) && !l.getBorrowDate().isAfter(to))
                .collect(Collectors.toList());
    }

    public List<Loan> findReturnedBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date range");
        }
        return loansById.values().stream()
                .filter(l -> l.getReturnDate() != null
                        && !l.getReturnDate().isBefore(from)
                        && !l.getReturnDate().isAfter(to))
                .collect(Collectors.toList());
    }

    public synchronized boolean deleteById(String id) {
        Loan removed = loansById.remove(id);
        if (removed == null) {
            return false;
        }
        List<String> memberLoans = loansByMember.get(removed.getMemberId());
        if (memberLoans != null) {
            memberLoans.remove(id);
        }
        List<String> bookLoans = loansByBook.get(removed.getBookId());
        if (bookLoans != null) {
            bookLoans.remove(id);
        }
        return true;
    }

    public long count() {
        return loansById.size();
    }

    public long countActive() {
        return loansById.values().stream().filter(Loan::isActive).count();
    }

    public long countOverdue() {
        return findOverdue().size();
    }

    public double totalFinesAccrued() {
        return loansById.values().stream()
                .mapToDouble(Loan::getFineAmount)
                .sum();
    }

    public boolean existsById(String id) {
        return id != null && loansById.containsKey(id);
    }

    public void clear() {
        loansById.clear();
        loansByMember.clear();
        loansByBook.clear();
    }
}
