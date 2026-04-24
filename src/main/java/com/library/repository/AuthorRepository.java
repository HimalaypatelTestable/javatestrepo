package com.library.repository;

import com.library.model.Author;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AuthorRepository {

    private final Map<String, Author> authorsById;

    public AuthorRepository() {
        this.authorsById = new ConcurrentHashMap<>();
    }

    public synchronized Author save(Author author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        authorsById.put(author.getId(), author);
        return author;
    }

    public Optional<Author> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(authorsById.get(id));
    }

    public List<Author> findAll() {
        return new ArrayList<>(authorsById.values());
    }

    public List<Author> findByNameContains(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            return Collections.emptyList();
        }
        String needle = fragment.toLowerCase();
        return authorsById.values().stream()
                .filter(a -> a.getDisplayName().toLowerCase().contains(needle)
                        || a.getFullName().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }

    public List<Author> findByNationality(String nationality) {
        if (nationality == null) {
            return Collections.emptyList();
        }
        return authorsById.values().stream()
                .filter(a -> nationality.equalsIgnoreCase(a.getNationality()))
                .collect(Collectors.toList());
    }

    public List<Author> findByGenre(String genre) {
        if (genre == null) {
            return Collections.emptyList();
        }
        return authorsById.values().stream()
                .filter(a -> a.writesInGenre(genre))
                .collect(Collectors.toList());
    }

    public List<Author> findLivingAuthors() {
        return authorsById.values().stream()
                .filter(Author::isAlive)
                .collect(Collectors.toList());
    }

    public List<Author> findVerified() {
        return authorsById.values().stream()
                .filter(Author::isVerified)
                .collect(Collectors.toList());
    }

    public List<Author> findMostProlific(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return authorsById.values().stream()
                .sorted(Comparator.comparingInt(Author::getBookCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Author> findAwardWinners() {
        return authorsById.values().stream()
                .filter(a -> !a.getAwards().isEmpty())
                .collect(Collectors.toList());
    }

    public List<Author> findBornBetween(int fromYear, int toYear) {
        if (fromYear > toYear) {
            throw new IllegalArgumentException("fromYear must be <= toYear");
        }
        return authorsById.values().stream()
                .filter(a -> a.getBirthDate() != null)
                .filter(a -> {
                    int year = a.getBirthDate().getYear();
                    return year >= fromYear && year <= toYear;
                })
                .collect(Collectors.toList());
    }

    public synchronized boolean deleteById(String id) {
        return authorsById.remove(id) != null;
    }

    public long count() {
        return authorsById.size();
    }

    public long countVerified() {
        return authorsById.values().stream().filter(Author::isVerified).count();
    }

    public long countLiving() {
        return authorsById.values().stream().filter(Author::isAlive).count();
    }

    public boolean existsById(String id) {
        return id != null && authorsById.containsKey(id);
    }

    public List<String> distinctNationalities() {
        return authorsById.values().stream()
                .map(Author::getNationality)
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> distinctGenres() {
        return authorsById.values().stream()
                .flatMap(a -> a.getGenres().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Map<String, Long> countByNationality() {
        return authorsById.values().stream()
                .filter(a -> a.getNationality() != null && !a.getNationality().isBlank())
                .collect(Collectors.groupingBy(Author::getNationality, Collectors.counting()));
    }

    public void clear() {
        authorsById.clear();
    }
}
