package com.library.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Book {

    public enum Status {
        AVAILABLE,
        BORROWED,
        RESERVED,
        LOST,
        UNDER_REPAIR,
        ARCHIVED
    }

    private final String id;
    private String isbn;
    private String title;
    private final List<Author> authors;
    private Publisher publisher;
    private Category category;
    private int publicationYear;
    private int numberOfCopies;
    private int availableCopies;
    private String language;
    private int pageCount;
    private double price;
    private LocalDate addedDate;
    private Status status;
    private String shelfLocation;
    private double averageRating;
    private int ratingCount;

    public Book(String id, String isbn, String title) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Book id cannot be null or blank");
        }
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be null or blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.authors = new ArrayList<>();
        this.numberOfCopies = 1;
        this.availableCopies = 1;
        this.language = "English";
        this.addedDate = LocalDate.now();
        this.status = Status.AVAILABLE;
        this.averageRating = 0.0;
        this.ratingCount = 0;
    }

    public String getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be null or blank");
        }
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        this.title = title;
    }

    public List<Author> getAuthors() {
        return Collections.unmodifiableList(authors);
    }

    public void addAuthor(Author author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        if (!authors.contains(author)) {
            authors.add(author);
        }
    }

    public boolean removeAuthor(Author author) {
        return authors.remove(author);
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        if (publicationYear < 1000 || publicationYear > LocalDate.now().getYear() + 1) {
            throw new IllegalArgumentException("Invalid publication year: " + publicationYear);
        }
        this.publicationYear = publicationYear;
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public void setNumberOfCopies(int numberOfCopies) {
        if (numberOfCopies < 0) {
            throw new IllegalArgumentException("Number of copies cannot be negative");
        }
        int borrowed = this.numberOfCopies - this.availableCopies;
        this.numberOfCopies = numberOfCopies;
        this.availableCopies = Math.max(0, numberOfCopies - borrowed);
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language == null ? "English" : language;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        if (pageCount < 0) {
            throw new IllegalArgumentException("Page count cannot be negative");
        }
        this.pageCount = pageCount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public LocalDate getAddedDate() {
        return addedDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status == null ? Status.AVAILABLE : status;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void addRating(double rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        double total = averageRating * ratingCount + rating;
        ratingCount += 1;
        averageRating = total / ratingCount;
    }

    public boolean isAvailable() {
        return availableCopies > 0 && status == Status.AVAILABLE;
    }

    public boolean borrow() {
        if (!isAvailable()) {
            return false;
        }
        availableCopies -= 1;
        if (availableCopies == 0) {
            status = Status.BORROWED;
        }
        return true;
    }

    public boolean returnCopy() {
        if (availableCopies >= numberOfCopies) {
            return false;
        }
        availableCopies += 1;
        if (availableCopies > 0 && status == Status.BORROWED) {
            status = Status.AVAILABLE;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Book{id='" + id + "', title='" + title + "', isbn='" + isbn +
                "', available=" + availableCopies + "/" + numberOfCopies + "}";
    }
}
