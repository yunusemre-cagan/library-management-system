package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // For formatting date in toString
import java.util.Objects;

/**
 * Represents a book in the library system.
 * Contains details such as ISBN, title, author, page count, stock information, etc.
 */
public class Book {
    // --- Fields ---
    private String isbn;            // International Standard Book Number, unique identifier for the book
    private String title;           // Title of the book
    private String authorName;      // Name of the book's author
    private String publisher;       // Publisher of the book
    // private LocalDate publicationDate; // REMOVED: Replaced with pageCount

    private int pageCount;          // NEW: Number of pages in the book

    private String category;        // Category or genre of the book (e.g., Fiction, Science, History)
    private int totalStock;         // Total number of copies of this book owned by the library
    private int availableStock;     // Number of copies currently available for borrowing
    private String description;     // A brief description or summary of the book (optional)

    // Annotation for Jackson to correctly serialize/deserialize LocalDateTime to/from JSON string
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dateAdded; // Date and time when the book was added to the system

    // --- Constructors ---

    /**
     * Default constructor.
     * Required by Jackson ObjectMapper for JSON deserialization.
     */
    public Book() {
        // Jackson needs a no-arg constructor
    }

    /**
     * Constructs a new Book object with specified details.
     * The available stock is initially set to the total stock.
     * The dateAdded field is set to the current date and time.
     *
     * @param isbn        The ISBN of the book.
     * @param title       The title of the book.
     * @param authorName  The author's name.
     * @param publisher   The publisher's name.
     * @param pageCount   The number of pages in the book.
     * @param category    The category of the book.
     * @param totalStock  The total number of copies of this book.
     * @param description A description of the book (can be null or empty).
     */
    public Book(String isbn, String title, String authorName, String publisher,
                int pageCount, String category, int totalStock,
                String description) {
        this.isbn = isbn;
        this.title = title;
        this.authorName = authorName;
        this.publisher = publisher;
        this.pageCount = pageCount; // Assigned
        this.category = category;
        this.totalStock = totalStock;
        this.availableStock = totalStock; // Initially, all stock is available
        this.description = description;
        this.dateAdded = LocalDateTime.now(); // Set to current date and time upon creation
    }

    // --- Getters and Setters ---
    // Standard getter and setter methods for all fields to allow access and modification.

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        // Basic validation can be added here if needed, e.g., not exceeding totalStock
        this.availableStock = availableStock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }


    // --- Stock Management Methods ---

    /**
     * Decreases the available stock of the book by one.
     * Does not allow available stock to go below zero.
     */
    public void decreaseAvailableStock() {
        if (this.availableStock > 0) {
            this.availableStock--;
        }
    }

    /**
     * Increases the available stock of the book by one.
     * Does not allow available stock to exceed total stock.
     */
    public void increaseAvailableStock() {
        if (this.availableStock < this.totalStock) {
            this.availableStock++;
        }
    }

    // --- Overridden Methods ---

    /**
     * Returns a string representation of the Book object, formatted for readability.
     *
     * @return A string containing the book's details.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------\n");
        sb.append(" Title: ").append(title != null ? title : "N/A").append("\n");
        sb.append(" Author: ").append(authorName != null ? authorName : "N/A").append("\n");
        sb.append(" ISBN: ").append(isbn != null ? isbn : "N/A").append("\n");
        sb.append(" Publisher: ").append(publisher != null ? publisher : "N/A").append("\n");
        sb.append(" Page Count: ").append(pageCount).append("\n");
        sb.append(" Category: ").append(category != null ? category : "N/A").append("\n");
        sb.append(" Total Stock: ").append(totalStock).append("\n");
        sb.append(" Available Stock: ").append(availableStock).append("\n");
        if (description != null && !description.isEmpty()) {
            sb.append(" Description: ").append(description).append("\n");
        }
        // Format dateAdded for better readability in toString, if not null
        sb.append(" Date Added: ").append(dateAdded != null ? dateAdded.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A").append("\n");
        sb.append("------------------------------------");
        return sb.toString();
    }

    /**
     * Compares this Book object to another object for equality.
     * Two books are considered equal if their ISBNs are the same (case-insensitive for robustness, though ISBNs are usually case-sensitive).
     *
     * @param o The object to compare with.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check for same instance
        if (o == null || getClass() != o.getClass()) return false; // Check for null and class type
        Book book = (Book) o; // Cast to Book type
        // Equality is based on the ISBN field
        return Objects.equals(isbn, book.isbn);
    }

    /**
     * Returns a hash code value for the Book object.
     * The hash code is based on the ISBN field.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        // Hash code generation based on the ISBN field
        return Objects.hash(isbn);
    }
}