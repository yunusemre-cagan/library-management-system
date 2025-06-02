package org.example.model.enums;

/**
 * Represents the possible statuses of a book borrowing record.
 * This enum is used to track whether a book is currently borrowed,
 * has been returned, or is overdue.
 */
public enum BorrowingStatus {
    /**
     * Indicates that the book is currently borrowed by a user and has not yet been returned.
     */
    BORROWED,

    /**
     * Indicates that the book was borrowed and has since been successfully returned to the library.
     */
    RETURNED,

    /**
     * Indicates that the book is currently borrowed by a user, and its due date has passed.
     * The book has not yet been returned.
     */
    OVERDUE
}