package com.library.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Category {

    private final String id;
    private String name;
    private String description;
    private Category parent;
    private final List<Category> subCategories;
    private final List<String> bookIds;
    private int displayOrder;
    private boolean active;
    private String iconCode;
    private String colorHex;
    private int popularityScore;

    public Category(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Category id cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be null or blank");
        }
        this.id = id;
        this.name = name;
        this.subCategories = new ArrayList<>();
        this.bookIds = new ArrayList<>();
        this.displayOrder = 0;
        this.active = true;
        this.popularityScore = 0;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        if (parent == this) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
        if (parent != null && parent.isDescendantOf(this)) {
            throw new IllegalArgumentException("Cannot create cyclic category hierarchy");
        }
        if (this.parent != null) {
            this.parent.subCategories.remove(this);
        }
        this.parent = parent;
        if (parent != null && !parent.subCategories.contains(this)) {
            parent.subCategories.add(this);
        }
    }

    public boolean isDescendantOf(Category other) {
        if (other == null) {
            return false;
        }
        Category current = this.parent;
        while (current != null) {
            if (current.equals(other)) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    public List<Category> getSubCategories() {
        return Collections.unmodifiableList(subCategories);
    }

    public void addSubCategory(Category subCategory) {
        if (subCategory == null) {
            throw new IllegalArgumentException("Sub category cannot be null");
        }
        subCategory.setParent(this);
    }

    public boolean removeSubCategory(Category subCategory) {
        if (subCategories.remove(subCategory)) {
            subCategory.parent = null;
            return true;
        }
        return false;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return subCategories.isEmpty();
    }

    public int getDepth() {
        int depth = 0;
        Category current = this.parent;
        while (current != null) {
            depth += 1;
            current = current.parent;
        }
        return depth;
    }

    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
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
            popularityScore += 1;
        }
    }

    public boolean removeBookId(String bookId) {
        return bookIds.remove(bookId);
    }

    public int getDirectBookCount() {
        return bookIds.size();
    }

    public int getTotalBookCount() {
        int total = bookIds.size();
        for (Category sub : subCategories) {
            total += sub.getTotalBookCount();
        }
        return total;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        if (colorHex != null && !colorHex.matches("#[0-9A-Fa-f]{6}")) {
            throw new IllegalArgumentException("Invalid color hex: " + colorHex);
        }
        this.colorHex = colorHex;
    }

    public int getPopularityScore() {
        return popularityScore;
    }

    public void incrementPopularity() {
        popularityScore += 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        return Objects.equals(id, ((Category) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Category{id='" + id + "', path='" + getFullPath() +
                "', directBooks=" + bookIds.size() + ", subCategories=" + subCategories.size() + "}";
    }
}
