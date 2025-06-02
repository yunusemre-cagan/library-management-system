package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
// JsonDeserializer and JsonSerializer imports are not directly used here as annotations
// are pointing to specific classes (LocalDateDeserializer, LocalDateSerializer).
// So, they can be removed if not used elsewhere in this file for other purposes.
// import com.fasterxml.jackson.databind.JsonDeserializer;
// import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.example.model.enums.BorrowingStatus; // Ensure this enum is correctly placed and imported

import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // For formatting date in toString
import java.util.Objects;

/**
 * Represents a record of a book borrowing transaction in the library system.
 * It links a book (via ISBN) to a user (via User ID) and tracks borrowing dates and status.
 */
public class BorrowingRecord {
    // --- Fields ---
    private String recordId;    // Unique identifier for this borrowing record (e.g., a UUID)
    private String bookIsbn;    // ISBN of the book that was borrowed
    private String userId;      // ID of the user who borrowed the book

    // Annotations for Jackson to correctly serialize/deserialize LocalDate to/from JSON string
    // using the "yyyy-MM-dd" pattern.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate borrowDate;   // The date when the book was borrowed

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate dueDate;      // The date when the book is expected to be returned

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate returnDate;   // The actual date when the book was returned (null if not yet returned)

    private BorrowingStatus status; // The current status of the borrowing (e.g., BORROWED, RETURNED, OVERDUE)

    // --- Constructors ---

    /**
     * Default constructor.
     * Required by Jackson ObjectMapper for JSON deserialization.
     */
    public BorrowingRecord() {
        // Jackson needs a no-arg constructor
    }

    /**
     * Constructs a new BorrowingRecord when a book is borrowed.
     * Initializes the status to BORROWED and the returnDate to null.
     *
     * @param recordId   The unique ID for this borrowing record.
     * @param bookIsbn   The ISBN of the borrowed book.
     * @param userId     The ID of the user borrowing the book.
     * @param borrowDate The date the book was borrowed.
     * @param dueDate    The date the book is due to be returned.
     */
    public BorrowingRecord(String recordId, String bookIsbn, String userId, LocalDate borrowDate, LocalDate dueDate) {
        this.recordId = recordId;
        this.bookIsbn = bookIsbn;
        this.userId = userId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = BorrowingStatus.BORROWED; // Default status when a book is borrowed
        this.returnDate = null;                 // Not returned yet at the time of borrowing
    }

    // --- Getters and Setters ---
    // Standard getter and setter methods for all fields.

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BorrowingStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowingStatus status) {
        this.status = status;
    }

    // --- Overridden Methods ---

    /**
     * Returns a string representation of the BorrowingRecord object, formatted for readability.
     * Dates are formatted as "dd.MM.yyyy".
     *
     * @return A string containing the borrowing record's details.
     */
    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // Date formatter for display
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------\n");
        sb.append(" Record ID: ").append(recordId != null ? recordId : "N/A").append("\n");
        sb.append(" Book ISBN: ").append(bookIsbn != null ? bookIsbn : "N/A").append("\n");
        sb.append(" User ID: ").append(userId != null ? userId : "N/A").append("\n");
        sb.append(" Borrow Date: ").append(borrowDate != null ? borrowDate.format(dtf) : "N/A").append("\n");
        sb.append(" Due Date: ").append(dueDate != null ? dueDate.format(dtf) : "N/A").append("\n");
        sb.append(" Return Date: ").append(returnDate != null ? returnDate.format(dtf) : "N/A").append("\n");
        sb.append(" Status: ").append(status != null ? status : "N/A").append("\n");
        sb.append("------------------------------------");
        return sb.toString();
    }

    /**
     * Compares this BorrowingRecord object to another object for equality.
     * Two records are considered equal if their recordIds are the same.
     *
     * @param o The object to compare with.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check for same instance
        if (o == null || getClass() != o.getClass()) return false; // Check for null and class type
        BorrowingRecord that = (BorrowingRecord) o; // Cast to BorrowingRecord type
        // Equality is based on the recordId field
        return Objects.equals(recordId, that.recordId);
    }

    /**
     * Returns a hash code value for the BorrowingRecord object.
     * The hash code is based on the recordId field.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        // Hash code generation based on the recordId field
        // Note: Original code used Objects.hashCode(recordId) which is fine for a single field.
        // For multiple fields, Objects.hash(field1, field2, ...) is used.
        // Here, recordId is a String, so Objects.hash(recordId) is equivalent to recordId != null ? recordId.hashCode() : 0
        // if recordId can be null. If recordId is guaranteed non-null, recordId.hashCode() is also an option.
        // Objects.hash handles nulls gracefully.
        return Objects.hash(recordId);
    }
}