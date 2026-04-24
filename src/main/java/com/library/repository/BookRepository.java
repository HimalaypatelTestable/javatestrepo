package com.library.repository;

import com.library.model.Book;
import com.library.model.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BookRepository {

    private final Map<String, Book> booksById;
    private final Map<String, String> isbnIndex;
    private final Map<String, List<String>> titleIndex;
    private final Map<String, List<String>> categoryIndex;
    private final Map<String, List<String>> authorIndex;

    public BookRepository() {
        this.booksById = new ConcurrentHashMap<>();
        this.isbnIndex = new ConcurrentHashMap<>();
        this.titleIndex = new HashMap<>();
        this.categoryIndex = new HashMap<>();
        this.authorIndex = new HashMap<>();
    }

    public synchronized Book save(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        Book existing = booksById.get(book.getId());
        if (existing != null && !existing.getIsbn().equals(book.getIsbn())) {
            isbnIndex.remove(existing.getIsbn());
        }
        booksById.put(book.getId(), book);
        isbnIndex.put(book.getIsbn(), book.getId());
        indexBook(book);
        return book;
    }

    private void indexBook(Book book) {
        String titleKey = normalize(book.getTitle());
        titleIndex.computeIfAbsent(titleKey, k -> new ArrayList<>());
        if (!titleIndex.get(titleKey).contains(book.getId())) {
            titleIndex.get(titleKey).add(book.getId());
        }
        if (book.getCategory() != null) {
            String catKey = book.getCategory().getId();
            categoryIndex.computeIfAbsent(catKey, k -> new ArrayList<>());
            if (!categoryIndex.get(catKey).contains(book.getId())) {
                categoryIndex.get(catKey).add(book.getId());
            }
        }
        book.getAuthors().forEach(author -> {
            String authorKey = author.getId();
            authorIndex.computeIfAbsent(authorKey, k -> new ArrayList<>());
            if (!authorIndex.get(authorKey).contains(book.getId())) {
                authorIndex.get(authorKey).add(book.getId());
            }
        });
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().trim();
    }

    public Optional<Book> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(booksById.get(id));
    }

    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null) {
            return Optional.empty();
        }
        String id = isbnIndex.get(isbn);
        return id == null ? Optional.empty() : findById(id);
    }

    public List<Book> findAll() {
        return new ArrayList<>(booksById.values());
    }

    public List<Book> findByTitleContains(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            return Collections.emptyList();
        }
        String needle = normalize(fragment);
        return booksById.values().stream()
                .filter(b -> normalize(b.getTitle()).contains(needle))
                .collect(Collectors.toList());
    }

    public List<Book> findByCategory(Category category) {
        if (category == null) {
            return Collections.emptyList();
        }
        List<String> ids = categoryIndex.getOrDefault(category.getId(), Collections.emptyList());
        return ids.stream().map(booksById::get).filter(b -> b != null).collect(Collectors.toList());
    }

    public List<Book> findByAuthorId(String authorId) {
        if (authorId == null) {
            return Collections.emptyList();
        }
        List<String> ids = authorIndex.getOrDefault(authorId, Collections.emptyList());
        return ids.stream().map(booksById::get).filter(b -> b != null).collect(Collectors.toList());
    }

    public List<Book> findAvailable() {
        return booksById.values().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Book> findByStatus(Book.Status status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return booksById.values().stream()
                .filter(b -> b.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Book> findByPublicationYearRange(int fromYear, int toYear) {
        if (fromYear > toYear) {
            throw new IllegalArgumentException("fromYear must be <= toYear");
        }
        return booksById.values().stream()
                .filter(b -> b.getPublicationYear() >= fromYear && b.getPublicationYear() <= toYear)
                .collect(Collectors.toList());
    }

    public List<Book> findTopRated(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return booksById.values().stream()
                .filter(b -> b.getRatingCount() > 0)
                .sorted(Comparator.comparingDouble(Book::getAverageRating).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public synchronized boolean deleteById(String id) {
        Book removed = booksById.remove(id);
        if (removed == null) {
            return false;
        }
        isbnIndex.remove(removed.getIsbn());
        String titleKey = normalize(removed.getTitle());
        if (titleIndex.containsKey(titleKey)) {
            titleIndex.get(titleKey).remove(id);
        }
        if (removed.getCategory() != null) {
            List<String> catIds = categoryIndex.get(removed.getCategory().getId());
            if (catIds != null) {
                catIds.remove(id);
            }
        }
        removed.getAuthors().forEach(a -> {
            List<String> authorIds = authorIndex.get(a.getId());
            if (authorIds != null) {
                authorIds.remove(id);
            }
        });
        return true;
    }

    public long count() {
        return booksById.size();
    }

    public long countAvailable() {
        return booksById.values().stream().filter(Book::isAvailable).count();
    }

    public boolean existsById(String id) {
        return id != null && booksById.containsKey(id);
    }

    public boolean existsByIsbn(String isbn) {
        return isbn != null && isbnIndex.containsKey(isbn);
    }

    public void clear() {
        booksById.clear();
        isbnIndex.clear();
        titleIndex.clear();
        categoryIndex.clear();
        authorIndex.clear();
    }
}
