package com.library.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Publisher {

    private final String id;
    private String name;
    private String country;
    private String city;
    private String address;
    private String email;
    private String phone;
    private String website;
    private int foundedYear;
    private final List<String> bookIds;
    private final List<String> specialties;
    private boolean active;
    private String taxId;
    private double averageBookPrice;

    public Publisher(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Publisher id cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Publisher name cannot be null or blank");
        }
        this.id = id;
        this.name = name;
        this.bookIds = new ArrayList<>();
        this.specialties = new ArrayList<>();
        this.active = true;
        this.averageBookPrice = 0.0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getFoundedYear() {
        return foundedYear;
    }

    public void setFoundedYear(int foundedYear) {
        int currentYear = LocalDate.now().getYear();
        if (foundedYear < 1400 || foundedYear > currentYear) {
            throw new IllegalArgumentException("Invalid founded year: " + foundedYear);
        }
        this.foundedYear = foundedYear;
    }

    public int getYearsInBusiness() {
        if (foundedYear <= 0) {
            return 0;
        }
        return LocalDate.now().getYear() - foundedYear;
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

    public List<String> getSpecialties() {
        return Collections.unmodifiableList(specialties);
    }

    public void addSpecialty(String specialty) {
        if (specialty != null && !specialty.isBlank() && !specialties.contains(specialty)) {
            specialties.add(specialty);
        }
    }

    public boolean removeSpecialty(String specialty) {
        return specialties.remove(specialty);
    }

    public boolean hasSpecialty(String specialty) {
        if (specialty == null) {
            return false;
        }
        return specialties.stream().anyMatch(s -> s.equalsIgnoreCase(specialty));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public double getAverageBookPrice() {
        return averageBookPrice;
    }

    public void setAverageBookPrice(double averageBookPrice) {
        if (averageBookPrice < 0) {
            throw new IllegalArgumentException("Average price cannot be negative");
        }
        this.averageBookPrice = averageBookPrice;
    }

    public String getLocation() {
        StringBuilder sb = new StringBuilder();
        if (city != null && !city.isBlank()) {
            sb.append(city);
        }
        if (country != null && !country.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Publisher)) return false;
        return Objects.equals(id, ((Publisher) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Publisher{id='" + id + "', name='" + name + "', location='" + getLocation() +
                "', books=" + bookIds.size() + ", active=" + active + "}";
    }
}
