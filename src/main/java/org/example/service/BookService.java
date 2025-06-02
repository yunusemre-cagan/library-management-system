package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference; // For generic type handling with Jackson
import org.example.model.Book;                       // Book data model
import org.example.util.JsonUtil;                    // Utility for JSON operations

import java.io.IOException;                         // For handling file I/O exceptions
import java.time.LocalDateTime;                     // For setting the date when a book is added
import java.util.ArrayList;                       // For creating new lists, e.g., in case of errors
import java.util.List;                            // For using List collections
import java.util.Optional;                        // For handling potentially absent values (e.g., find operations)
import java.util.stream.Collectors;                 // For collecting stream results into a List

/**
 * Service class for managing book-related operations.
 * This includes loading books from a JSON file, saving them,
 * adding, finding, updating, and deleting books.
 */
public class BookService {
    // Path to the JSON file where book data is stored.
    private static final String BOOKS_FILE_PATH = "data/books.json";
    // In-memory list to hold the books. This list is synchronized with the JSON file.
    private List<Book> books;

    /**
     * Constructor for BookService.
     * Initializes the service by loading existing books from the JSON file.
     */
    public BookService() {
        this.books = loadBooks(); // Load books when the service is instantiated
    }

    /**
     * Loads the list of books from the JSON file specified by BOOKS_FILE_PATH.
     * Uses JsonUtil to perform the deserialization.
     *
     * @return A list of Book objects. Returns an empty list if the file doesn't exist,
     * is empty, or an error occurs during loading.
     */
    public List<Book> loadBooks() { // Changed to public for potential external use/testing, though typically private/protected
        try {
            // Read the list of books using a TypeReference to handle the generic List<Book>
            return JsonUtil.readListFromJsonFile(BOOKS_FILE_PATH, new TypeReference<List<Book>>() {
            });
        } catch (IOException e) {
            // Print an error message if loading fails
            System.err.println(("Error loading books from file: " + e.getMessage())); // Corrected typo "form" to "from"
            return new ArrayList<>(); // Return an empty list to prevent NullPointerExceptions later
        }
    }

    /**
     * Saves the current in-memory list of books to the JSON file.
     * Uses JsonUtil to perform the serialization.
     * This method is typically called after any modification to the books list (add, update, delete).
     */
    public void saveBooks() {
        try {
            JsonUtil.writeListToJsonFile(this.books, BOOKS_FILE_PATH);
        } catch (IOException e) {
            // Print an error message if saving fails
            System.err.println("Error saving books to file: " + e.getMessage());
        }
    }

    /**
     * Adds a new book to the library.
     * Checks for ISBN uniqueness before adding. Sets the 'dateAdded' timestamp.
     * Saves the updated list of books to the JSON file.
     *
     * @param book The Book object to be added.
     */
    public void addBook(Book book) {
        // Check if a book with the same ISBN already exists (case-insensitive)
        if (books.stream().anyMatch(b -> b.getIsbn().equalsIgnoreCase(book.getIsbn()))) {
            System.out.println("Error: Book with ISBN " + book.getIsbn() + " already exists.");
            return; // Do not add if duplicate ISBN
        }
        // Set the dateAdded to the current moment
        book.setDateAdded(LocalDateTime.now());
        this.books.add(book); // Add the new book to the in-memory list
        saveBooks();          // Persist the changes to the JSON file
        System.out.println("Book added successfully: " + book.getTitle());
    }

    /**
     * Retrieves a copy of all books currently in the library.
     *
     * @return A new list containing all Book objects. Modifying this returned list
     * will not affect the original list in the service.
     */
    public List<Book> getAllBooks() {
        // Return a copy to prevent external modification of the internal list
        return new ArrayList<>(this.books);
    }

    /**
     * Finds a book by its ISBN (case-insensitive).
     *
     * @param isbn The ISBN of the book to find.
     * @return An Optional containing the Book if found, otherwise an empty Optional.
     */
    public Optional<Book> findBookByIsbn(String isbn) {
        return this.books.stream()
                .filter(book -> book.getIsbn().equalsIgnoreCase(isbn)) // Filter by ISBN, ignoring case
                .findFirst(); // Return the first match, if any
    }

    /**
     * Finds books whose titles contain the given search string (case-insensitive).
     *
     * @param title The search string to match against book titles.
     * @return A list of Book objects مليونات titles match the search string.
     */
    public List<Book> findBooksByTitle(String title) {
        if (title == null || title.trim().isEmpty()) { // Handle empty or null search term
            return new ArrayList<>();
        }
        String searchTerm = title.toLowerCase(); // Convert search term to lower case for case-insensitive search
        return this.books.stream()
                .filter(book -> book.getTitle() != null && book.getTitle().toLowerCase().contains(searchTerm)) // Check for null title
                .collect(Collectors.toList()); // Collect matching books into a list
    }

    /**
     * Finds books whose author names contain the given search string (case-insensitive).
     *
     * @param authorName The search string to match against author names.
     * @return A list of Book objects whose authors match the search string.
     */
    public List<Book> findBooksByAuthor(String authorName) {
        if (authorName == null || authorName.trim().isEmpty()) { // Handle empty or null search term
            return new ArrayList<>();
        }
        String searchTerm = authorName.toLowerCase();
        return this.books.stream()
                .filter(book -> book.getAuthorName() != null && book.getAuthorName().toLowerCase().contains(searchTerm)) // Check for null author name
                .collect(Collectors.toList());
    }

    /**
     * Updates the details of an existing book.
     * The book to be updated is identified by its ISBN from the {@code updatedBook} object.
     *
     * @param updatedBook The Book object containing the new details. The ISBN must match an existing book.
     * @return True if the update was successful, false otherwise (e.g., book not found).
     */
    public boolean updateBook(Book updatedBook) { // Parameter name changed for clarity
        // Find the existing book by ISBN from the updatedBook object
        Optional<Book> existingBookOpt = findBookByIsbn(updatedBook.getIsbn());
        if (existingBookOpt.isPresent()) {
            Book existingBook = existingBookOpt.get(); // Get the actual book object from Optional

            // Update the fields of the existing book with values from updatedBook
            existingBook.setTitle(updatedBook.getTitle());
            existingBook.setAuthorName(updatedBook.getAuthorName()); // Corrected: was (updateBook.getAuthorName())
            existingBook.setPublisher(updatedBook.getPublisher());
            existingBook.setPageCount(updatedBook.getPageCount());
            // POTENTIAL BUG: Setting category to authorName. Should be updatedBook.getCategory()
            // existingBook.setCategory(updateBook.getAuthorName()); // This seems to be a typo
            existingBook.setCategory(updatedBook.getCategory()); // CORRECTED
            existingBook.setTotalStock(updatedBook.getTotalStock());
            // Ensure available stock is valid (not more than total, not negative)
            existingBook.setAvailableStock(Math.min(updatedBook.getAvailableStock(), updatedBook.getTotalStock()));
            if (existingBook.getAvailableStock() < 0) {
                existingBook.setAvailableStock(0);
            }
            existingBook.setDescription(updatedBook.getDescription());
            // Note: dateAdded is typically not updated. It reflects when the book was first added.
            // If you intend to update it, you'd do: existingBook.setDateAdded(updatedBook.getDateAdded());

            saveBooks(); // Persist the changes
            System.out.println("Book updated: " + existingBook.getTitle());
            return true;
        }
        // If the book with the given ISBN was not found
        System.out.println("Error: Book with ISBN " + updatedBook.getIsbn() + " not found for update."); // Corrected "uptdate"
        return false;
    }

    /**
     * Deletes a book from the library based on its ISBN.
     *
     * @param isbn The ISBN of the book to be deleted.
     * @return True if the book was found and deleted, false otherwise.
     */
    public boolean deleteBookByIsbn(String isbn) {
        // Remove the book from the in-memory list if its ISBN matches (case-insensitive)
        boolean removed = this.books.removeIf(book -> book.getIsbn().equalsIgnoreCase(isbn));
        if (removed) {
            saveBooks(); // Persist the changes if a book was removed
            System.out.println("Book with ISBN " + isbn + " deleted successfully.");
        } else {
            System.out.println("Error: Book with ISBN " + isbn + " not found for deletion.");
        }
        return removed; // Return the status of the removal operation
    }
}