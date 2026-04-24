package com.library.app;

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Category;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.model.Publisher;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import com.library.service.ReportService;
import com.library.service.SearchService;
import com.library.util.DateUtils;
import com.library.util.IdGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class LibraryApp {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final AuthorRepository authorRepository;
    private final BookService bookService;
    private final MemberService memberService;
    private final LoanService loanService;
    private final SearchService searchService;
    private final ReportService reportService;
    private final IdGenerator idGenerator;

    public LibraryApp() {
        this.bookRepository = new BookRepository();
        this.memberRepository = new MemberRepository();
        this.loanRepository = new LoanRepository();
        this.authorRepository = new AuthorRepository();
        this.idGenerator = new IdGenerator();
        this.bookService = new BookService(bookRepository, authorRepository, idGenerator);
        this.memberService = new MemberService(memberRepository, idGenerator);
        this.loanService = new LoanService(loanRepository, bookRepository, memberRepository, idGenerator);
        this.searchService = new SearchService(bookRepository, authorRepository, memberRepository);
        this.reportService = new ReportService(bookRepository, memberRepository, loanRepository, authorRepository);
    }

    public static void main(String[] args) {
        LibraryApp app = new LibraryApp();
        app.seedData();
        app.runDemo();
    }

    public void seedData() {
        System.out.println("=== Seeding library data ===");

        Publisher orwellHouse = new Publisher(idGenerator.nextPublisherId(), "Orwell House");
        orwellHouse.setCountry("UK");
        orwellHouse.setCity("London");
        orwellHouse.setFoundedYear(1935);
        orwellHouse.addSpecialty("Literary Fiction");

        Publisher techBooks = new Publisher(idGenerator.nextPublisherId(), "TechBooks Press");
        techBooks.setCountry("USA");
        techBooks.setCity("San Francisco");
        techBooks.setFoundedYear(1998);
        techBooks.addSpecialty("Computer Science");

        Category fiction = new Category(idGenerator.nextCategoryId(), "Fiction");
        Category scifi = new Category(idGenerator.nextCategoryId(), "Science Fiction");
        scifi.setParent(fiction);
        Category tech = new Category(idGenerator.nextCategoryId(), "Technology");
        Category programming = new Category(idGenerator.nextCategoryId(), "Programming");
        programming.setParent(tech);

        Author orwell = new Author(idGenerator.nextAuthorId(), "George", "Orwell");
        orwell.setNationality("British");
        orwell.setBirthDate(LocalDate.of(1903, 6, 25));
        orwell.setDeathDate(LocalDate.of(1950, 1, 21));
        orwell.addGenre("Dystopian");
        orwell.addGenre("Political Fiction");
        orwell.setVerified(true);
        authorRepository.save(orwell);

        Author knuth = new Author(idGenerator.nextAuthorId(), "Donald", "Knuth");
        knuth.setNationality("American");
        knuth.setBirthDate(LocalDate.of(1938, 1, 10));
        knuth.addGenre("Computer Science");
        knuth.addAward("Turing Award");
        knuth.setVerified(true);
        authorRepository.save(knuth);

        Book nineteenEightyFour = bookService.createBook("9780451524935", "1984", 1949, 3);
        bookService.assignAuthor(nineteenEightyFour.getId(), orwell.getId());
        bookService.assignCategory(nineteenEightyFour.getId(), scifi);
        bookService.assignPublisher(nineteenEightyFour.getId(), orwellHouse);
        nineteenEightyFour.setPrice(15.99);
        nineteenEightyFour.setPageCount(328);
        bookRepository.save(nineteenEightyFour);

        Book animalFarm = bookService.createBook("9780451526342", "Animal Farm", 1945, 2);
        bookService.assignAuthor(animalFarm.getId(), orwell.getId());
        bookService.assignCategory(animalFarm.getId(), fiction);
        bookService.assignPublisher(animalFarm.getId(), orwellHouse);
        animalFarm.setPrice(12.50);
        bookRepository.save(animalFarm);

        Book taocp = bookService.createBook("9780201896831", "The Art of Computer Programming", 1968, 1);
        bookService.assignAuthor(taocp.getId(), knuth.getId());
        bookService.assignCategory(taocp.getId(), programming);
        bookService.assignPublisher(taocp.getId(), techBooks);
        taocp.setPrice(89.99);
        bookRepository.save(taocp);

        Member alice = memberService.registerMember("Alice", "Walker", "alice@example.com");
        memberService.updateContactInfo(alice.getId(), "+1-555-0100", "123 Main St");

        Member bob = memberService.registerMember("Bob", "Martin", "bob@example.com");
        memberService.upgradeMembership(bob.getId(), Member.MembershipType.PREMIUM);

        Member carol = memberService.registerMember("Carol", "Davis", "carol@example.com");
        memberService.upgradeMembership(carol.getId(), Member.MembershipType.STUDENT);

        System.out.println("Seeded " + bookRepository.count() + " books, "
                + memberRepository.count() + " members, "
                + authorRepository.count() + " authors.");
    }

    public void runDemo() {
        System.out.println("\n=== Running demo workflows ===");

        Member alice = memberService.findByEmail("alice@example.com").orElseThrow();
        Member bob = memberService.findByEmail("bob@example.com").orElseThrow();
        List<Book> available = bookService.findAvailable();
        if (available.isEmpty()) {
            System.out.println("No books available to borrow.");
            return;
        }

        Book first = available.get(0);
        Loan loan1 = loanService.borrowBook(alice.getId(), first.getId());
        System.out.println("Alice borrowed: " + first.getTitle() + " (due " + DateUtils.formatDisplay(loan1.getDueDate()) + ")");

        if (available.size() > 1) {
            Book second = available.get(1);
            Loan loan2 = loanService.borrowBook(bob.getId(), second.getId());
            System.out.println("Bob borrowed: " + second.getTitle());
            loanService.renewLoan(loan2.getId());
            System.out.println("Bob renewed loan — new due date: " + DateUtils.formatDisplay(loan2.getDueDate()));
        }

        bookService.rateBook(first.getId(), 4.5);
        bookService.rateBook(first.getId(), 5.0);

        Loan returned = loanService.returnBook(loan1.getId());
        System.out.println("Alice returned book. Fine: " + returned.getFineAmount());

        System.out.println("\n=== Search ===");
        List<Book> orwellBooks = searchService.searchBooks(
                new SearchService.BookFilter().titleFragment("animal"),
                SearchService.BookSort.TITLE_ASC);
        orwellBooks.forEach(b -> System.out.println("  - " + b.getTitle()));

        System.out.println("\n=== Reports ===");
        Map<String, Object> summary = reportService.inventorySummary();
        summary.forEach((k, v) -> System.out.println("  " + k + ": " + v));

        System.out.println("\nMost borrowed books:");
        reportService.mostBorrowedBooks(5).forEach(b -> System.out.println("  - " + b.getTitle()));

        System.out.println("\nTop authors by loans:");
        reportService.topAuthorsByLoans(5).forEach(a -> System.out.println("  - " + a.getDisplayName()));

        System.out.println("\nAverage loan duration: " + reportService.averageLoanDuration() + " days");
        System.out.println("Overdue rate: " + (reportService.overdueRate() * 100) + "%");
    }

    public BookService bookService() {
        return bookService;
    }

    public MemberService memberService() {
        return memberService;
    }

    public LoanService loanService() {
        return loanService;
    }

    public SearchService searchService() {
        return searchService;
    }

    public ReportService reportService() {
        return reportService;
    }
}
