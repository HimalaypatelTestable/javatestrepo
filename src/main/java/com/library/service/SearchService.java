package com.library.service;

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Member;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchService {

    public static class BookFilter {
        private String titleFragment;
        private String authorId;
        private String categoryId;
        private String language;
        private Integer fromYear;
        private Integer toYear;
        private Boolean onlyAvailable;
        private Double minRating;

        public BookFilter titleFragment(String v) { this.titleFragment = v; return this; }
        public BookFilter authorId(String v) { this.authorId = v; return this; }
        public BookFilter categoryId(String v) { this.categoryId = v; return this; }
        public BookFilter language(String v) { this.language = v; return this; }
        public BookFilter fromYear(Integer v) { this.fromYear = v; return this; }
        public BookFilter toYear(Integer v) { this.toYear = v; return this; }
        public BookFilter onlyAvailable(Boolean v) { this.onlyAvailable = v; return this; }
        public BookFilter minRating(Double v) { this.minRating = v; return this; }
    }

    public enum BookSort {
        TITLE_ASC,
        TITLE_DESC,
        YEAR_ASC,
        YEAR_DESC,
        RATING_DESC,
        AVAILABILITY_DESC
    }

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final MemberRepository memberRepository;

    public SearchService(BookRepository bookRepository,
                         AuthorRepository authorRepository,
                         MemberRepository memberRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "bookRepository");
        this.authorRepository = Objects.requireNonNull(authorRepository, "authorRepository");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository");
    }

    public List<Book> searchBooks(BookFilter filter, BookSort sort) {
        Stream<Book> stream = bookRepository.findAll().stream();
        if (filter == null) {
            filter = new BookFilter();
        }
        if (filter.titleFragment != null && !filter.titleFragment.isBlank()) {
            String needle = filter.titleFragment.toLowerCase();
            stream = stream.filter(b -> b.getTitle().toLowerCase().contains(needle));
        }
        if (filter.authorId != null) {
            final String authorId = filter.authorId;
            stream = stream.filter(b -> b.getAuthors().stream().anyMatch(a -> a.getId().equals(authorId)));
        }
        if (filter.categoryId != null) {
            final String categoryId = filter.categoryId;
            stream = stream.filter(b -> b.getCategory() != null && b.getCategory().getId().equals(categoryId));
        }
        if (filter.language != null) {
            final String language = filter.language;
            stream = stream.filter(b -> language.equalsIgnoreCase(b.getLanguage()));
        }
        if (filter.fromYear != null) {
            final int from = filter.fromYear;
            stream = stream.filter(b -> b.getPublicationYear() >= from);
        }
        if (filter.toYear != null) {
            final int to = filter.toYear;
            stream = stream.filter(b -> b.getPublicationYear() <= to);
        }
        if (Boolean.TRUE.equals(filter.onlyAvailable)) {
            stream = stream.filter(Book::isAvailable);
        }
        if (filter.minRating != null) {
            final double min = filter.minRating;
            stream = stream.filter(b -> b.getAverageRating() >= min);
        }
        return applySort(stream, sort).collect(Collectors.toList());
    }

    private Stream<Book> applySort(Stream<Book> stream, BookSort sort) {
        if (sort == null) {
            return stream;
        }
        switch (sort) {
            case TITLE_ASC:
                return stream.sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
            case TITLE_DESC:
                return stream.sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
            case YEAR_ASC:
                return stream.sorted(Comparator.comparingInt(Book::getPublicationYear));
            case YEAR_DESC:
                return stream.sorted(Comparator.comparingInt(Book::getPublicationYear).reversed());
            case RATING_DESC:
                return stream.sorted(Comparator.comparingDouble(Book::getAverageRating).reversed());
            case AVAILABILITY_DESC:
                return stream.sorted(Comparator.comparingInt(Book::getAvailableCopies).reversed());
            default:
                return stream;
        }
    }

    public List<Author> searchAuthors(String fragment) {
        return authorRepository.findByNameContains(fragment);
    }

    public List<Member> searchMembers(String fragment) {
        return memberRepository.findByNameContains(fragment);
    }

    public List<Book> recommendForMember(Member member, int limit) {
        if (member == null || limit <= 0) {
            return List.of();
        }
        List<Book> history = member.getLoanHistoryIds().stream()
                .map(bookRepository::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        if (history.isEmpty()) {
            return bookRepository.findTopRated(limit);
        }
        List<String> preferredCategoryIds = history.stream()
                .map(Book::getCategory)
                .filter(c -> c != null)
                .map(c -> c.getId())
                .distinct()
                .collect(Collectors.toList());
        return bookRepository.findAll().stream()
                .filter(Book::isAvailable)
                .filter(b -> !member.getLoanHistoryIds().contains(b.getId()))
                .filter(b -> b.getCategory() != null && preferredCategoryIds.contains(b.getCategory().getId()))
                .sorted(Comparator.comparingDouble(Book::getAverageRating).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Book> quickSearch(String term) {
        return searchBooks(new BookFilter().titleFragment(term), BookSort.TITLE_ASC);
    }

    public List<Book> findBooksByLanguage(String language) {
        if (language == null || language.isBlank()) {
            return List.of();
        }
        return bookRepository.findAll().stream()
                .filter(b -> language.equalsIgnoreCase(b.getLanguage()))
                .collect(Collectors.toList());
    }

    public List<Book> findBooksPublishedAfter(int year) {
        return bookRepository.findAll().stream()
                .filter(b -> b.getPublicationYear() > year)
                .sorted(Comparator.comparingInt(Book::getPublicationYear).reversed())
                .collect(Collectors.toList());
    }
}
