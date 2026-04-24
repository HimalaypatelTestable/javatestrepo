package com.library.service;

import com.library.exception.LibraryException;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Category;
import com.library.model.Publisher;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.util.IdGenerator;
import com.library.util.ValidationUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final IdGenerator idGenerator;

    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       IdGenerator idGenerator) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository");
        this.authorRepository = Objects.requireNonNull(authorRepository, "authorRepository");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    public Book createBook(String isbn, String title, int publicationYear, int numberOfCopies) {
        ValidationUtils.requireIsbn(isbn);
        ValidationUtils.requireNonBlank(title, "title");
        if (numberOfCopies < 1) {
            throw new IllegalArgumentException("Number of copies must be at least 1");
        }
        if (bookRepository.existsByIsbn(isbn)) {
            throw new LibraryException("Book with ISBN already exists: " + isbn);
        }
        String id = idGenerator.nextBookId();
        Book book = new Book(id, isbn, title);
        book.setPublicationYear(publicationYear);
        book.setNumberOfCopies(numberOfCopies);
        return bookRepository.save(book);
    }

    public Book updateTitle(String bookId, String newTitle) {
        Book book = requireBook(bookId);
        ValidationUtils.requireNonBlank(newTitle, "title");
        book.setTitle(newTitle);
        return bookRepository.save(book);
    }

    public Book assignAuthor(String bookId, String authorId) {
        Book book = requireBook(bookId);
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new LibraryException("Author not found: " + authorId));
        book.addAuthor(author);
        author.addBookId(book.getId());
        bookRepository.save(book);
        authorRepository.save(author);
        return book;
    }

    public Book unassignAuthor(String bookId, String authorId) {
        Book book = requireBook(bookId);
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new LibraryException("Author not found: " + authorId));
        book.removeAuthor(author);
        author.removeBookId(book.getId());
        bookRepository.save(book);
        authorRepository.save(author);
        return book;
    }

    public Book assignCategory(String bookId, Category category) {
        Book book = requireBook(bookId);
        if (book.getCategory() != null) {
            book.getCategory().removeBookId(book.getId());
        }
        book.setCategory(category);
        if (category != null) {
            category.addBookId(book.getId());
        }
        return bookRepository.save(book);
    }

    public Book assignPublisher(String bookId, Publisher publisher) {
        Book book = requireBook(bookId);
        if (book.getPublisher() != null && publisher != null
                && !book.getPublisher().getId().equals(publisher.getId())) {
            book.getPublisher().removeBookId(book.getId());
        }
        book.setPublisher(publisher);
        if (publisher != null) {
            publisher.addBookId(book.getId());
        }
        return bookRepository.save(book);
    }

    public Book addCopies(String bookId, int extraCopies) {
        if (extraCopies <= 0) {
            throw new IllegalArgumentException("Extra copies must be positive");
        }
        Book book = requireBook(bookId);
        book.setNumberOfCopies(book.getNumberOfCopies() + extraCopies);
        return bookRepository.save(book);
    }

    public Book removeCopies(String bookId, int copiesToRemove) {
        if (copiesToRemove <= 0) {
            throw new IllegalArgumentException("Copies to remove must be positive");
        }
        Book book = requireBook(bookId);
        int borrowed = book.getNumberOfCopies() - book.getAvailableCopies();
        int newTotal = book.getNumberOfCopies() - copiesToRemove;
        if (newTotal < borrowed) {
            throw new LibraryException("Cannot remove copies currently on loan");
        }
        book.setNumberOfCopies(newTotal);
        return bookRepository.save(book);
    }

    public Book rateBook(String bookId, double rating) {
        Book book = requireBook(bookId);
        book.addRating(rating);
        return bookRepository.save(book);
    }

    public Book markLost(String bookId) {
        Book book = requireBook(bookId);
        book.setStatus(Book.Status.LOST);
        return bookRepository.save(book);
    }

    public Book sendForRepair(String bookId) {
        Book book = requireBook(bookId);
        book.setStatus(Book.Status.UNDER_REPAIR);
        return bookRepository.save(book);
    }

    public Book restoreToShelf(String bookId) {
        Book book = requireBook(bookId);
        book.setStatus(Book.Status.AVAILABLE);
        return bookRepository.save(book);
    }

    public void deleteBook(String bookId) {
        Book book = requireBook(bookId);
        book.getAuthors().forEach(a -> a.removeBookId(book.getId()));
        if (book.getCategory() != null) {
            book.getCategory().removeBookId(book.getId());
        }
        if (book.getPublisher() != null) {
            book.getPublisher().removeBookId(book.getId());
        }
        bookRepository.deleteById(bookId);
    }

    public Optional<Book> findById(String bookId) {
        return bookRepository.findById(bookId);
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public List<Book> findAvailable() {
        return bookRepository.findAvailable();
    }

    public List<Book> findLowStock(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }
        return bookRepository.findAll().stream()
                .filter(b -> b.getAvailableCopies() <= threshold)
                .collect(Collectors.toList());
    }

    private Book requireBook(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new LibraryException("Book not found: " + bookId));
    }
}
