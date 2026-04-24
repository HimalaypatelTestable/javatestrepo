package com.library.service;

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReportService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final AuthorRepository authorRepository;

    public ReportService(BookRepository bookRepository,
                         MemberRepository memberRepository,
                         LoanRepository loanRepository,
                         AuthorRepository authorRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository);
        this.memberRepository = Objects.requireNonNull(memberRepository);
        this.loanRepository = Objects.requireNonNull(loanRepository);
        this.authorRepository = Objects.requireNonNull(authorRepository);
    }

    public Map<String, Object> inventorySummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalBooks", bookRepository.count());
        summary.put("availableBooks", bookRepository.countAvailable());
        summary.put("totalMembers", memberRepository.count());
        summary.put("activeMembers", memberRepository.countActive());
        summary.put("totalLoans", loanRepository.count());
        summary.put("activeLoans", loanRepository.countActive());
        summary.put("overdueLoans", loanRepository.countOverdue());
        summary.put("totalAuthors", authorRepository.count());
        summary.put("outstandingFines", memberRepository.totalOutstandingFines());
        summary.put("generatedAt", LocalDate.now().toString());
        return summary;
    }

    public List<Book> mostBorrowedBooks(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        Map<String, Long> counts = loanRepository.findAll().stream()
                .collect(Collectors.groupingBy(Loan::getBookId, Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> bookRepository.findById(e.getKey()).orElse(null))
                .filter(b -> b != null)
                .collect(Collectors.toList());
    }

    public List<Member> mostActiveMembers(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        Map<String, Long> counts = loanRepository.findAll().stream()
                .collect(Collectors.groupingBy(Loan::getMemberId, Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> memberRepository.findById(e.getKey()).orElse(null))
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    public Map<String, Long> loanCountByCategory() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Loan loan : loanRepository.findAll()) {
            Book book = bookRepository.findById(loan.getBookId()).orElse(null);
            if (book == null || book.getCategory() == null) {
                continue;
            }
            String key = book.getCategory().getName();
            counts.merge(key, 1L, Long::sum);
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    public double averageLoanDuration() {
        List<Loan> returned = loanRepository.findAll().stream()
                .filter(l -> l.getReturnDate() != null)
                .collect(Collectors.toList());
        if (returned.isEmpty()) {
            return 0.0;
        }
        double totalDays = returned.stream()
                .mapToLong(l -> ChronoUnit.DAYS.between(l.getBorrowDate(), l.getReturnDate()))
                .sum();
        return totalDays / returned.size();
    }

    public double overdueRate() {
        long total = loanRepository.count();
        if (total == 0) {
            return 0.0;
        }
        long overdue = loanRepository.findAll().stream()
                .filter(l -> l.getReturnDate() != null
                        && l.getReturnDate().isAfter(l.getDueDate()))
                .count();
        return (double) overdue / total;
    }

    public List<Author> topAuthorsByLoans(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        Map<String, Long> loansPerAuthor = new LinkedHashMap<>();
        for (Loan loan : loanRepository.findAll()) {
            Book book = bookRepository.findById(loan.getBookId()).orElse(null);
            if (book == null) continue;
            for (Author a : book.getAuthors()) {
                loansPerAuthor.merge(a.getId(), 1L, Long::sum);
            }
        }
        return loansPerAuthor.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> authorRepository.findById(e.getKey()).orElse(null))
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }

    public Map<String, Double> finesByMembershipType() {
        return memberRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        m -> m.getMembershipType().name(),
                        Collectors.summingDouble(Member::getOutstandingFine)));
    }

    public List<Book> neverBorrowed() {
        List<String> borrowedIds = loanRepository.findAll().stream()
                .map(Loan::getBookId)
                .distinct()
                .collect(Collectors.toList());
        return bookRepository.findAll().stream()
                .filter(b -> !borrowedIds.contains(b.getId()))
                .sorted(Comparator.comparing(Book::getAddedDate))
                .collect(Collectors.toList());
    }
}
