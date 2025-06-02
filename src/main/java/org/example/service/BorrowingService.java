package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference; // For generic type handling with Jackson
import org.example.model.Book;                       // Book data model
import org.example.model.BorrowingRecord;            // BorrowingRecord data model
import org.example.model.User;                       // User data model
import org.example.model.enums.BorrowingStatus;      // Enum for borrowing status
import org.example.util.JsonUtil;                    // Utility for JSON operations

import java.io.IOException;                         // For handling file I/O exceptions
import java.time.LocalDate;                         // For handling dates
import java.util.ArrayList;                       // For creating new lists
import java.util.List;                            // For using List collections
import java.util.Optional;                        // For handling potentially absent values
import java.util.UUID;                            // For generating unique record IDs
import java.util.stream.Collectors;                 // For collecting stream results into a List

/**
 * Service class for managing book borrowing and returning operations.
 * It interacts with BookService and UserService to validate data and update related entities.
 * Borrowing records are persisted to a JSON file.
 */
public class BorrowingService {
    // Path to the JSON file where borrowing records are stored.
    private static final String BORROWINGS_FILE_PATH = "data/borrowings.json";
    // In-memory list to hold the borrowing records.
    private List<BorrowingRecord> borrowingRecords;
    // Dependencies on other services for book and user information.
    private BookService bookService;
    private UserService userService;

    /**
     * Constructor for BorrowingService.
     * Initializes the service by loading existing borrowing records from the JSON file
     * and injecting dependencies for BookService and UserService.
     *
     * @param bookService An instance of BookService to manage book data.
     * @param userService An instance of UserService to manage user data.
     */
    public BorrowingService(BookService bookService, UserService userService) {
        // It's generally better to load records AFTER dependencies are set,
        // but if loadBorrowingRecords doesn't use these dependencies, this order is fine.
        // If loadBorrowingRecords might need to validate against books/users,
        // then set dependencies first. For now, this is okay.
        this.borrowingRecords = loadBorrowingRecords();
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Loads the list of borrowing records from the JSON file.
     *
     * @return A list of BorrowingRecord objects. Returns an empty list if loading fails.
     */
    private List<BorrowingRecord> loadBorrowingRecords() {
        try {
            return JsonUtil.readListFromJsonFile(BORROWINGS_FILE_PATH, new TypeReference<List<BorrowingRecord>>() {
            });
        } catch (IOException e) {
            System.err.println("Error loading borrowing records from file: " + e.getMessage());
            return new ArrayList<>(); // Return empty list on error
        }
    }

    /**
     * Saves the current in-memory list of borrowing records to the JSON file.
     */
    private void saveBorrowingRecords() {
        try {
            JsonUtil.writeListToJsonFile(this.borrowingRecords, BORROWINGS_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error saving borrowing records to file: " + e.getMessage());
        }
    }

    /**
     * Processes a book borrowing request.
     * Validates the user and book, checks book availability, and ensures the user hasn't already borrowed the same book.
     * If successful, creates a new borrowing record, updates book stock, and saves records.
     *
     * @param userEmail The email of the user borrowing the book.
     * @param bookIsbn  The ISBN of the book to be borrowed.
     * @param loanDays  The number of days for which the book is loaned.
     * @return True if the book was borrowed successfully, false otherwise.
     */
    public boolean borrowBook(String userEmail, String bookIsbn, int loanDays) {
        // Validate user
        Optional<User> userOpt = userService.findUserByEmail(userEmail);
        if (userOpt.isEmpty()) {
            System.out.println("Error: User with email " + userEmail + " not found.");
            return false;
        }

        // Validate book
        Optional<Book> bookOpt = bookService.findBookByIsbn(bookIsbn);
        if (bookOpt.isEmpty()) {
            System.out.println("Error: Book with ISBN " + bookIsbn + " not found.");
            return false;
        }

        Book book = bookOpt.get();
        // Check if book is in stock
        if (book.getAvailableStock() <= 0) {
            System.out.println("Error: Book '" + book.getTitle() + "' is currently out of stock.");
            return false;
        }

        // Check if the user has already borrowed this specific book and not returned it
        boolean alreadyBorrowed = borrowingRecords.stream()
                .anyMatch(r -> r.getUserId().equals(userOpt.get().getUserId()) &&
                        r.getBookIsbn().equals(bookIsbn) &&
                        r.getStatus() == BorrowingStatus.BORROWED);
        if (alreadyBorrowed) {
            System.out.println("Error: User " + userEmail + " has already borrowed book " + bookIsbn + " and not returned it yet.");
            return false;
        }

        // Create new borrowing record
        String recordId = UUID.randomUUID().toString(); // Generate a unique ID for the record
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(loanDays); // Calculate due date

        BorrowingRecord record = new BorrowingRecord(recordId, bookIsbn, userOpt.get().getUserId(), borrowDate, dueDate);
        borrowingRecords.add(record); // Add to in-memory list

        // Update book stock
        book.decreaseAvailableStock();
        bookService.updateBook(book); // This will also save the books.json

        saveBorrowingRecords(); // Save the updated borrowing records to borrowings.json
        System.out.println("Book '" + book.getTitle() + "' borrowed successfully by user " + userEmail + ". Due date: " + dueDate);
        return true;
    }

    /**
     * Processes a book return.
     * Finds the active borrowing record for the given user and book, updates its status and return date.
     * Increases the available stock of the returned book.
     *
     * @param bookIsbn  The ISBN of the book being returned.
     * @param userEmail The email of the user returning the book.
     * @return True if the book was returned successfully, false otherwise (e.g., no active record found).
     */
    public boolean returnBook(String bookIsbn, String userEmail) {
        // Validate user
        Optional<User> userOpt = userService.findUserByEmail(userEmail);
        if (userOpt.isEmpty()) {
            System.out.println("Error: User with email " + userEmail + " not found.");
            return false;
        }

        // Find the active borrowing record for this book by this user
        Optional<BorrowingRecord> recordOpt = borrowingRecords.stream()
                .filter(r -> r.getBookIsbn().equalsIgnoreCase(bookIsbn) &&
                        r.getUserId().equals(userOpt.get().getUserId()) &&
                        r.getStatus() == BorrowingStatus.BORROWED) // Only find currently BORROWED records
                .findFirst();

        if (recordOpt.isEmpty()) {
            System.out.println("Error: No active borrowing record found for book ISBN " + bookIsbn + " by user " + userEmail);
            return false;
        }

        BorrowingRecord record = recordOpt.get();
        // Update the record
        record.setReturnDate(LocalDate.now());
        record.setStatus(BorrowingStatus.RETURNED);

        // Update book stock
        Optional<Book> bookOpt = bookService.findBookByIsbn(bookIsbn);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.increaseAvailableStock();
            bookService.updateBook(book); // Persist book stock changes
        } else {
            // This case should ideally not happen if data integrity is maintained
            System.err.println("Critical Error: Book with ISBN " + bookIsbn + " not found during return process, but was borrowed.");
            // Log this error. Stock cannot be increased if book is not found.
        }

        saveBorrowingRecords(); // Save updated borrowing records
        System.out.println("Book with ISBN " + bookIsbn + " returned successfully by user " + userEmail);
        return true;
    }

    /**
     * Retrieves a copy of all borrowing records in the system.
     *
     * @return A new list containing all BorrowingRecord objects.
     */
    public List<BorrowingRecord> getAllBorrowingRecords() {
        return new ArrayList<>(this.borrowingRecords); // Return a copy
    }

    /**
     * Retrieves all books currently borrowed by a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of BorrowingRecord objects for books currently borrowed by the user.
     */
    public List<BorrowingRecord> getBorrowedBooksByUser(String userId) {
        return this.borrowingRecords.stream()
                .filter(record -> record.getUserId().equals(userId) && record.getStatus() == BorrowingStatus.BORROWED)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all books that are currently overdue.
     * A book is considered overdue if its status is BORROWED and its due date is before today.
     *
     * @return A list of BorrowingRecord objects for overdue books.
     */
    public List<BorrowingRecord> getOverdueBooks() {
        LocalDate today = LocalDate.now();
        return this.borrowingRecords.stream()
                .filter(record -> record.getStatus() == BorrowingStatus.BORROWED &&
                        record.getDueDate() != null && // Ensure dueDate is not null
                        record.getDueDate().isBefore(today))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a specific book is currently borrowed by any user.
     *
     * @param bookIsbn The ISBN of the book to check.
     * @return True if the book is currently borrowed, false otherwise.
     */
    public boolean isBookCurrentlyBorrowed(String bookIsbn) {
        return this.borrowingRecords.stream()
                .anyMatch(record -> record.getBookIsbn().equalsIgnoreCase(bookIsbn) &&
                        record.getStatus() == BorrowingStatus.BORROWED);
    }
}