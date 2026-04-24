package com.library.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Author {

    private final String id;
    private String firstName;
    private String lastName;
    private String penName;
    private String nationality;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String biography;
    private String website;
    private final List<String> bookIds;
    private final List<String> awards;
    private final List<String> genres;
    private boolean verified;

    public Author(String id, String firstName, String lastName) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Author id cannot be null or blank");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bookIds = new ArrayList<>();
        this.awards = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.verified = false;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDisplayName() {
        return penName != null && !penName.isBlank() ? penName : getFullName();
    }

    public String getPenName() {
        return penName;
    }

    public void setPenName(String penName) {
        this.penName = penName;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
        this.birthDate = birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        if (deathDate != null && birthDate != null && deathDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("Death date cannot be before birth date");
        }
        this.deathDate = deathDate;
    }

    public boolean isAlive() {
        return deathDate == null;
    }

    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        LocalDate endDate = deathDate != null ? deathDate : LocalDate.now();
        return Period.between(birthDate, endDate).getYears();
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<String> getBookIds() {
        return Collections.unmodifiableList(bookIds);
    }

    public void addBookId(String bookId) {
        if (bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Book id cannot be null or blank");
        }
        if (!bookIds.contains(bookId)) {
            bookIds.add(bookId);
        }
    }

    public boolean removeBookId(String bookId) {
        return bookIds.remove(bookId);
    }

    public int getBookCount() {
        return bookIds.size();
    }

    public List<String> getAwards() {
        return Collections.unmodifiableList(awards);
    }

    public void addAward(String award) {
        if (award != null && !award.isBlank() && !awards.contains(award)) {
            awards.add(award);
        }
    }

    public List<String> getGenres() {
        return Collections.unmodifiableList(genres);
    }

    public void addGenre(String genre) {
        if (genre != null && !genre.isBlank() && !genres.contains(genre)) {
            genres.add(genre);
        }
    }

    public boolean writesInGenre(String genre) {
        return genres.stream().anyMatch(g -> g.equalsIgnoreCase(genre));
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean hasPublishedIn(int year) {
        if (birthDate == null) {
            return false;
        }
        if (year < birthDate.getYear()) {
            return false;
        }
        if (deathDate != null && year > deathDate.getYear()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author)) return false;
        return Objects.equals(id, ((Author) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Author{id='" + id + "', name='" + getDisplayName() +
                "', books=" + bookIds.size() + ", verified=" + verified + "}";
    }
}
