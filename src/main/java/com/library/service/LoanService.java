package com.library.service;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.util.IdGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final IdGenerator idGenerator;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       MemberRepository memberRepository,
                       IdGenerator idGenerator) {
        this.loanRepository = Objects.requireNonNull(loanRepository, "loanRepository");
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public synchronized Loan borrowBook(String memberId, String bookId) {
        Member member = requireMember(memberId);
        Book book = requireBook(bookId);
        if (!member.canBorrow()) {
            throw new LibraryException("Member is not eligible to borrow: " + memberId);
        }
        if (!book.isAvailable()) {
            throw new LibraryException("Book is not available: " + bookId);
        }
        if (!book.borrow()) {
            throw new LibraryException("Failed to reserve a copy of book: " + bookId);
        }
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(member.getMembershipType().getLoanDurationDays());
        String loanId = idGenerator.nextLoanId();
        Loan loan = new Loan(loanId, bookId, memberId, today, dueDate);
        try {
            member.addActiveLoan(loanId);
        } catch (RuntimeException e) {
            book.returnCopy();
            throw new LibraryException("Cannot assign loan to member: " + e.getMessage());
        }
        loanRepository.save(loan);
        bookRepository.save(book);
        memberRepository.save(member);
        return loan;
    }

    public synchronized Loan returnBook(String loanId) {
        return returnBookOn(loanId, LocalDate.now());
    }

    public synchronized Loan returnBookOn(String loanId, LocalDate returnDate) {
        Loan loan = requireLoan(loanId);
        if (!loan.isActive()) {
            throw new LibraryException("Loan is not active: " + loanId);
        }
        Book book = requireBook(loan.getBookId());
        Member member = requireMember(loan.getMemberId());
        double fine = loan.returnOn(returnDate, book.getPrice());
        if (fine > 0) {
            member.addFine(fine);
        }
        book.returnCopy();
        member.completeLoan(loanId);
        loanRepository.save(loan);
        bookRepository.save(book);
        memberRepository.save(member);
        return loan;
    }

    public synchronized Loan renewLoan(String loanId) {
        Loan loan = requireLoan(loanId);
        Member member = requireMember(loan.getMemberId());
        if (!loan.canRenew()) {
            throw new LibraryException("Loan cannot be renewed: " + loanId);
        }
        int days = member.getMembershipType().getLoanDurationDays();
        if (!loan.renew(days)) {
            throw new LibraryException("Renewal refused: " + loanId);
        }
        loanRepository.save(loan);
        return loan;
    }

    public synchronized Loan reportLost(String loanId) {
        Loan loan = requireLoan(loanId);
        if (!loan.isActive()) {
            throw new LibraryException("Loan is not active: " + loanId);
        }
        Book book = requireBook(loan.getBookId());
        Member member = requireMember(loan.getMemberId());
        double fine = loan.markLost(book.getPrice());
        member.addFine(fine);
        member.completeLoan(loanId);
        book.setStatus(Book.Status.LOST);
        loanRepository.save(loan);
        bookRepository.save(book);
        memberRepository.save(member);
        return loan;
    }

    public void refreshOverdueLoans() {
        loanRepository.findActive().forEach(loan -> {
            Loan.Status before = loan.getStatus();
            loan.refreshOverdueStatus();
            if (loan.getStatus() != before) {
                loanRepository.save(loan);
            }
        });
    }

    public List<Loan> findLoansForMember(String memberId) {
        requireMember(memberId);
        return loanRepository.findByMemberId(memberId);
    }

    public List<Loan> findActiveLoansForMember(String memberId) {
        requireMember(memberId);
        return loanRepository.findActiveByMemberId(memberId);
    }

    public List<Loan> findLoansForBook(String bookId) {
        requireBook(bookId);
        return loanRepository.findByBookId(bookId);
    }

    public List<Loan> findOverdueLoans() {
        return loanRepository.findOverdue();
    }

    public List<Loan> findLoansDueSoon(int days) {
        return loanRepository.findDueWithinDays(days);
    }

    private Loan requireLoan(String loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new LibraryException("Loan not found: " + loanId));
    }

    private Book requireBook(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new LibraryException("Book not found: " + bookId));
    }

    private Member requireMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new LibraryException("Member not found: " + memberId));
    }
}
