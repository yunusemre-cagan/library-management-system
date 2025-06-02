package org.example;

import org.example.model.Book;
import org.example.model.User;
import org.example.model.BorrowingRecord;
import org.example.model.enums.MembershipStatus;
import org.example.service.BookService;
import org.example.service.BorrowingService;
import org.example.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    // Scanner for user input, shared across all methods
    private static final Scanner scanner = new Scanner(System.in);
    // Service instances to manage books, users, and borrowings
    private static final BookService bookService = new BookService();
    private static final UserService userService = new UserService();
    private static final BorrowingService borrowingService = new BorrowingService(bookService, userService);
    // Removed dateFormatter as it's not directly used globally now (formatting is done inline)
    // private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
     * Main entry point of the Library Management System application.
     * Initializes the system and runs the main interaction loop.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("Welcome to Cagan's Library Management System!");

        boolean running = true; // Controls the main application loop
        while (running) {
            printMainMenu(); // Display the main menu options
            int choice = getUserIntInput("Enter your choice (0-3): "); // Get user's menu choice

            // Process user's choice
            switch (choice) {
                case 1:
                    manageBooksMenu(); // Navigate to book management
                    break;
                case 2:
                    manageUsersMenu(); // Navigate to user management
                    break;
                case 3:
                    manageBorrowingsMenu(); // Navigate to borrowing management
                    break;
                case 0:
                    running = false; // Exit the application loop
                    System.out.println("Exiting application. See you next time with new books!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again."); // Handle invalid input
            }
        }
        scanner.close(); // Close the scanner when the application exits
    }

    /**
     * Prints the main menu options to the console.
     */
    private static void printMainMenu() {
        System.out.println("\n|--- Cagan's Library - Main Menu ---|");
        System.out.println("1- Manage Books");
        System.out.println("2- Manage Users");
        System.out.println("3- Manage Borrowings");
        System.out.println("0- Exit Application");
    }

    // --- Book Management ---

    /**
     * Displays the book management sub-menu and handles user choices for book operations.
     */
    private static void manageBooksMenu() {
        boolean back = false; // Controls the book management menu loop
        while (!back) {
            System.out.println("\n--- Book Management ---");
            System.out.println("1- Add New Book");
            System.out.println("2- View All Books");
            System.out.println("3- Find Book by ISBN");
            System.out.println("4- Find Books by Title");
            System.out.println("5- Update Book");
            System.out.println("6- Delete Book");
            System.out.println("0- Back to Main Menu");
            int choice = getUserIntInput("Enter your choice (0-6): ");

            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    viewAllBooks();
                    break;
                case 3:
                    findBookByIsbn();
                    break;
                case 4:
                    findBooksByTitle();
                    break;
                case 5:
                    updateBook();
                    break;
                case 6:
                    deleteBook();
                    break;
                case 0:
                    back = true;
                    break; // Exit to the main menu
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Handles the process of adding a new book to the library.
     * Prompts the user for book details and calls the BookService to add the book.
     */
    private static void addBook() {
        System.out.println("\n--- Add New Book ---");
        // Get ISBN, ensuring it's not empty and doesn't already exist
        String isbn = getUserStringInput("Enter ISBN: ", false);
        if (isbn == null) return; // User might have an option to cancel, or input failed
        if (bookService.findBookByIsbn(isbn).isPresent()) {
            System.out.println("Book with this ISBN already exists.");
            return;
        }
        // Get other mandatory book details
        String title = getUserStringInput("Enter Title: ", false);
        if (title == null) return;
        String author = getUserStringInput("Enter Author Name: ", false);
        if (author == null) return;

        // Get optional or validated details
        String publisher = getUserStringInput("Enter Publisher: ", true); // Allow empty
        int pageCount = getUserIntInput("Enter Page Count (must be > 0): ");
        while (pageCount <= 0) { // Validate page count
            System.out.println("Page count must be a positive number.");
            pageCount = getUserIntInput("Enter Page Count (must be > 0): ");
        }
        String category = getUserStringInput("Enter Category: ", true); // Allow empty
        int totalStock = getUserIntInput("Enter Total Stock (must be >= 0): ");
        while (totalStock < 0) { // Validate total stock
            System.out.println("Total stock cannot be negative.");
            totalStock = getUserIntInput("Enter Total Stock (must be >= 0): ");
        }
        String description = getUserStringInput("Enter Description (optional): ", true); // Allow empty

        // Create and add the new book
        Book newBook = new Book(isbn, title, author, publisher, pageCount, category, totalStock, description);
        bookService.addBook(newBook);
    }

    /**
     * Displays all books currently in the library in a formatted table.
     */
    private static void viewAllBooks() {
        System.out.println("\n--- All Books in the Library ---");
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books available in the library at the moment.");
            return;
        }
        // Print table header
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-15s | %-30s | %-25s | %-20s | %-5s | %-15s | %-7s | %-60s\n",
                "ISBN", "TITLE", "AUTHOR", "PUBLISHER", "PAGES", "CATEGORY", "STOCK", "DESCRIPTION");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        // Print each book's details in a formatted row
        for (Book book : books) {
            System.out.printf("%-15s | %-30.30s | %-25.25s | %-20.20s | %-5d | %-15.15s | %-3d/%-3d | %-60.60s\n",
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthorName(),
                    book.getPublisher(),
                    book.getPageCount(),
                    book.getCategory(),
                    book.getAvailableStock(), // AVL
                    book.getTotalStock(),   // TTL
                    book.getDescription() != null ? book.getDescription() : ""); // Handle null description
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("STOCK: Available/Total");
    }

    /**
     * Prompts the user for an ISBN and displays the details of the found book.
     */
    private static void findBookByIsbn() {
        System.out.println("\n--- Find Book by ISBN ---");
        String isbn = getUserStringInput("Enter ISBN to search: ", false);
        if (isbn == null) return;
        Optional<Book> bookOpt = bookService.findBookByIsbn(isbn);
        // Display book details if found, otherwise show a not found message
        bookOpt.ifPresentOrElse(
                book -> printBookDetails(book), // Use a helper to print details
                () -> System.out.println("Book with ISBN " + isbn + " not found.")
        );
    }

    /**
     * Helper method to print detailed information of a single book.
     *
     * @param book The book to display.
     */
    private static void printBookDetails(Book book) {
        System.out.println("------------------------------------");
        System.out.println(" Title: " + book.getTitle());
        System.out.println(" Author: " + book.getAuthorName());
        System.out.println(" ISBN: " + book.getIsbn());
        System.out.println(" Publisher: " + book.getPublisher());
        System.out.println(" Page Count: " + book.getPageCount());
        System.out.println(" Category: " + book.getCategory());
        System.out.println(" Total Stock: " + book.getTotalStock());
        System.out.println(" Available Stock: " + book.getAvailableStock());
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            System.out.println(" Description: " + book.getDescription());
        }
        System.out.println(" Date Added: " + (book.getDateAdded() != null ? book.getDateAdded().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A"));
        System.out.println("------------------------------------");
    }


    /**
     * Prompts the user for a title (or part of it) and displays matching books.
     */
    private static void findBooksByTitle() {
        System.out.println("\n--- Find Books by Title ---");
        String title = getUserStringInput("Enter title (or part of it) to search: ", true); // Allow empty for broader search if desired
        List<Book> books = bookService.findBooksByTitle(title);
        if (books.isEmpty()) {
            System.out.println("No books found with title containing '" + title + "'.");
            return;
        }
        System.out.println("Found " + books.size() + " book(s):");
        // For consistency, you could use the same table format as viewAllBooks or printBookDetails for each book
        books.forEach(Main::printBookDetails); // Using the helper method for detailed view
    }

    /**
     * Handles the process of updating an existing book's details.
     */
    private static void updateBook() {
        System.out.println("\n--- Update Book ---");
        String isbnToUpdate = getUserStringInput("Enter ISBN of the book to update: ", false);
        if (isbnToUpdate == null) return;
        Optional<Book> bookOpt = bookService.findBookByIsbn(isbnToUpdate);

        if (bookOpt.isEmpty()) {
            System.out.println("Book with ISBN " + isbnToUpdate + " not found.");
            return;
        }

        Book existingBook = bookOpt.get();
        System.out.println("Updating book: " + existingBook.getTitle() + " (ISBN: " + existingBook.getIsbn() + ")");
        System.out.println("Current details:");
        printBookDetails(existingBook); // Use helper to show current details
        System.out.println("------------------------------------");
        System.out.println("Enter new information (press Enter to keep current value, '*' to make field empty for optional fields):");

        // Get updated information, allowing user to keep current values or clear optional ones
        String newTitle = getUserStringInputWithDefaultOrClear("New Title: ", existingBook.getTitle(), false);
        if (newTitle == null) return; // Should not happen if not allowing empty for mandatory
        String newAuthor = getUserStringInputWithDefaultOrClear("New Author: ", existingBook.getAuthorName(), false);
        if (newAuthor == null) return;
        String newPublisher = getUserStringInputWithDefaultOrClear("New Publisher: ", existingBook.getPublisher(), true);

        int newPageCount = getUserIntInputWithDefault("New Page Count: ", existingBook.getPageCount());
        while (newPageCount <= 0 && newPageCount != existingBook.getPageCount()) {
            System.out.println("Page count must be a positive number if changed from current.");
            newPageCount = getUserIntInputWithDefault("New Page Count: ", existingBook.getPageCount());
        }

        String newCategory = getUserStringInputWithDefaultOrClear("New Category: ", existingBook.getCategory(), true);
        int newTotalStock = getUserIntInputWithDefault("New Total Stock: ", existingBook.getTotalStock());
        while (newTotalStock < 0 && newTotalStock != existingBook.getTotalStock()) {
            System.out.println("Total stock cannot be negative if changed from current.");
            newTotalStock = getUserIntInputWithDefault("New Total Stock: ", existingBook.getTotalStock());
        }

        int newAvailableStock;
        while (true) {
            newAvailableStock = getUserIntInputWithDefault("New Available Stock: ", existingBook.getAvailableStock());
            if (newAvailableStock > newTotalStock) {
                System.out.println("Available stock cannot be greater than new total stock (" + newTotalStock + "). Please re-enter.");
            } else if (newAvailableStock < 0) {
                System.out.println("Available stock cannot be negative. Please re-enter.");
            } else {
                break;
            }
        }

        String newDescription = getUserStringInputWithDefaultOrClear("New Description: ", existingBook.getDescription(), true);

        // Create an updated Book object. ISBN and dateAdded are not changed.
        Book updatedBook = new Book(
                existingBook.getIsbn(), newTitle, newAuthor, newPublisher,
                newPageCount, newCategory, newTotalStock, newDescription
        );
        updatedBook.setAvailableStock(newAvailableStock); // Set available stock separately
        updatedBook.setDateAdded(existingBook.getDateAdded()); // Preserve original added date

        bookService.updateBook(updatedBook);
    }

    /**
     * Helper method to display details of a book found by ISBN.
     * This was refactored from the original findBookByIsbn to be reusable.
     *
     * @param isbn The ISBN of the book to display.
     */
    private static void findBookByIsbnInternal(String isbn) { // Renamed to avoid conflict, now used by updateBook
        bookService.findBookByIsbn(isbn).ifPresent(Main::printBookDetails);
    }


    /**
     * Handles the process of deleting a book from the library.
     * Warns if the book is currently borrowed.
     */
    private static void deleteBook() {
        System.out.println("\n--- Delete Book ---");
        String isbn = getUserStringInput("Enter ISBN of the book to delete: ", false);
        if (isbn == null) return;

        // Check if the book is currently borrowed before deleting
        if (borrowingService.isBookCurrentlyBorrowed(isbn)) {
            System.out.println("Warning: This book (ISBN: " + isbn + ") is currently borrowed by a user.");
            System.out.println("If you delete it, borrowing records might become inconsistent.");
            String confirmation = getUserStringInput("Are you sure you want to delete it? (yes/no): ", false);
            if (confirmation == null || !confirmation.equalsIgnoreCase("yes")) {
                System.out.println("Deletion cancelled.");
                return;
            }
        }
        bookService.deleteBookByIsbn(isbn);
    }


    // --- User Management ---

    /**
     * Displays the user management sub-menu and handles user choices for user operations.
     */
    private static void manageUsersMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- User Management ---");
            System.out.println("1- Add New User");
            System.out.println("2- View All Users");
            System.out.println("3- Find User by Email");
            System.out.println("4- Update User");
            System.out.println("5- Delete User");
            System.out.println("0- Back to Main Menu");
            int choice = getUserIntInput("Enter your choice (0-5): ");

            switch (choice) {
                case 1:
                    addUser();
                    break;
                case 2:
                    viewAllUsers();
                    break;
                case 3:
                    findUserByEmail();
                    break;
                case 4:
                    updateUser();
                    break;
                case 5:
                    deleteUser();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Handles the process of adding a new user to the system.
     */
    private static void addUser() {
        System.out.println("\n--- Add New User ---");
        // Get email, ensuring it's not empty, valid format, and doesn't already exist
        String email = getUserStringInput("Enter Email (cannot be empty, e.g., user@example.com): ", false);
        if (email == null) return;
        if (!isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }
        if (userService.findUserByEmail(email).isPresent()) {
            System.out.println("User with this email already exists.");
            return;
        }
        // Get other mandatory user details
        String firstName = getUserStringInput("Enter First Name: ", false);
        if (firstName == null) return;
        String lastName = getUserStringInput("Enter Last Name: ", false);
        if (lastName == null) return;

        // Get optional details
        String phone = getUserStringInput("Enter Phone Number (e.g., +xxxxxxxxxxx): ", true);
        String address = getUserStringInput("Enter Address: ", true);

        // User ID will be auto-generated by UserService or User constructor
        User newUser = new User(null, firstName, lastName, phone, email, address);
        userService.addUser(newUser);
    }

    /**
     * Displays all registered users in a formatted table.
     */
    private static void viewAllUsers() {
        System.out.println("\n--- All Registered Users ---");
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users registered in the system.");
            return;
        }
        // Print table header
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-38s | %-25s | %-30s | %-15s | %-30s | %-15s | %-12s\n",
                "USER ID", "NAME", "EMAIL", "PHONE", "ADDRESS", "REG. DATE", "STATUS");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        // Print each user's details
        for (User user : users) {
            System.out.printf("%-38s | %-25.25s | %-30.30s | %-15s | %-30.30s | %-15s | %-12s\n",
                    user.getUserId(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getEmail(),
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : "", // Handle null phone
                    user.getAddress() != null ? user.getAddress() : "",       // Handle null address
                    user.getRegistrationDate() != null ? user.getRegistrationDate().format(dtf) : "N/A",
                    user.getStatus());
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * Prompts the user for an email and displays the details of the found user.
     */
    private static void findUserByEmail() {
        System.out.println("\n--- Find User by Email ---");
        String email = getUserStringInput("Enter Email to search (cannot be empty): ", false);
        if (email == null) return;
        Optional<User> userOpt = userService.findUserByEmail(email);
        userOpt.ifPresentOrElse(
                user -> printUserDetails(user), // Use a helper to print details
                () -> System.out.println("User with email " + email + " not found.")
        );
    }

    /**
     * Helper method to print detailed information of a single user.
     *
     * @param user The user to display.
     */
    private static void printUserDetails(User user) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        System.out.println("------------------------------------");
        System.out.println(" User ID: " + user.getUserId());
        System.out.println(" Name: " + user.getFirstName() + " " + user.getLastName());
        System.out.println(" Email: " + user.getEmail());
        System.out.println(" Phone: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "N/A"));
        System.out.println(" Address: " + (user.getAddress() != null ? user.getAddress() : "N/A"));
        System.out.println(" Registration Date: " + (user.getRegistrationDate() != null ? user.getRegistrationDate().format(dtf) : "N/A"));
        System.out.println(" Membership Status: " + user.getStatus());
        System.out.println("------------------------------------");
    }

    /**
     * Handles the process of updating an existing user's details.
     */
    private static void updateUser() {
        System.out.println("\n--- Update User ---");
        String emailToUpdate = getUserStringInput("Enter Email of the user to update (cannot be empty): ", false);
        if (emailToUpdate == null) return;
        Optional<User> userOpt = userService.findUserByEmail(emailToUpdate);

        if (userOpt.isEmpty()) {
            System.out.println("User with email " + emailToUpdate + " not found.");
            return;
        }
        User existingUser = userOpt.get();
        System.out.println("Updating user: " + existingUser.getFirstName() + " " + existingUser.getLastName());
        printUserDetails(existingUser); // Show current details using helper
        System.out.println("------------------------------------");
        System.out.println("Enter new information (press Enter to keep current value, '*' to make field empty for optional fields):");

        String firstName = getUserStringInputWithDefaultOrClear("New First Name: ", existingUser.getFirstName(), false);
        if (firstName == null && (existingUser.getFirstName() != null && !existingUser.getFirstName().isEmpty()))
            return; // Prevent clearing mandatory field if it was filled

        String lastName = getUserStringInputWithDefaultOrClear("New Last Name: ", existingUser.getLastName(), false);
        if (lastName == null && (existingUser.getLastName() != null && !existingUser.getLastName().isEmpty())) return;

        String phone = getUserStringInputWithDefaultOrClear("New Phone: ", existingUser.getPhoneNumber(), true);

        String newEmail = getUserStringInputWithDefaultOrClear("New Email: ", existingUser.getEmail(), false);
        if (newEmail == null && (existingUser.getEmail() != null && !existingUser.getEmail().isEmpty())) return;
        if (newEmail != null && !newEmail.equalsIgnoreCase(existingUser.getEmail()) && !isValidEmail(newEmail)) {
            System.out.println("Invalid new email format.");
            return;
        }
        // Check if the new email (if changed) is already in use by another user
        if (newEmail != null && !newEmail.equalsIgnoreCase(existingUser.getEmail()) && userService.findUserByEmail(newEmail).isPresent()) {
            System.out.println("Error: New email " + newEmail + " is already in use by another user.");
            return;
        }

        String address = getUserStringInputWithDefaultOrClear("New Address: ", existingUser.getAddress(), true);

        // Update membership status
        System.out.println("Current Status: " + existingUser.getStatus());
        System.out.println("Select new status (1-ACTIVE, 2-PASSIVE, 3-SUSPENDED, 4-EXPIRED, Enter to keep current):");
        MembershipStatus newStatus = existingUser.getStatus();
        String statusChoiceStr = scanner.nextLine().trim();
        if (!statusChoiceStr.isEmpty()) {
            try {
                int statusChoice = Integer.parseInt(statusChoiceStr);
                switch (statusChoice) {
                    case 1:
                        newStatus = MembershipStatus.ACTIVE;
                        break;
                    case 2:
                        newStatus = MembershipStatus.PASSIVE;
                        break;
                    case 3:
                        newStatus = MembershipStatus.SUSPENDED;
                        break;
                    case 4:
                        newStatus = MembershipStatus.EXPIRED;
                        break;
                    default:
                        System.out.println("Invalid status choice, keeping current.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input for status, keeping current.");
            }
        }

        // Create updated User object. UserID and registrationDate are not changed.
        User updatedUser = new User(existingUser.getUserId(), firstName, lastName, phone, newEmail, address);
        updatedUser.setStatus(newStatus);
        updatedUser.setRegistrationDate(existingUser.getRegistrationDate()); // Preserve original registration date

        userService.updateUser(updatedUser);
    }

    /**
     * Helper method to display details of a user found by email.
     * Refactored for reusability.
     *
     * @param email The email of the user to display.
     */
    private static void findUserByEmailInternal(String email) { // Renamed, now used by updateUser
        userService.findUserByEmail(email).ifPresent(Main::printUserDetails);
    }


    /**
     * Handles the process of deleting a user from the system.
     * Warns if the user has active borrowings.
     */
    private static void deleteUser() {
        System.out.println("\n--- Delete User ---");
        String email = getUserStringInput("Enter Email of the user to delete (cannot be empty): ", false);
        if (email == null) return;
        Optional<User> userOpt = userService.findUserByEmail(email);
        if (userOpt.isPresent()) {
            String userIdToDelete = userOpt.get().getUserId();
            // Check for active borrowings before deleting
            List<BorrowingRecord> activeBorrows = borrowingService.getBorrowedBooksByUser(userIdToDelete);
            if (!activeBorrows.isEmpty()) {
                System.out.println("Warning: This user (Email: " + email + ") has " + activeBorrows.size() + " book(s) currently borrowed:");
                activeBorrows.forEach(record ->
                        bookService.findBookByIsbn(record.getBookIsbn())
                                .ifPresent(book -> System.out.println("  - " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")"))
                );
                System.out.println("Please ensure all books are returned before deleting the user.");
                String confirmation = getUserStringInput("Are you sure you want to delete this user? This will NOT delete their borrowing history. (yes/no): ", false);
                if (confirmation == null || !confirmation.equalsIgnoreCase("yes")) {
                    System.out.println("Deletion cancelled.");
                    return;
                }
                // Note: In a real system, you might want to handle borrowing history differently (e.g., archive)
            }
            userService.deleteUserById(userIdToDelete);
        } else {
            System.out.println("User with email " + email + " not found.");
        }
    }

    // --- Borrowing Management ---

    /**
     * Displays the borrowing management sub-menu and handles user choices.
     */
    private static void manageBorrowingsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Borrowing Management ---");
            System.out.println("1- Borrow a Book");
            System.out.println("2- Return a Book");
            System.out.println("3- View All Borrowing Records");
            System.out.println("4- View Books Borrowed by User (by Email)");
            System.out.println("5- View Overdue Books");
            System.out.println("0- Back to Main Menu");
            int choice = getUserIntInput("Enter your choice (0-5): ");

            switch (choice) {
                case 1:
                    borrowBook();
                    break;
                case 2:
                    returnBook();
                    break;
                case 3:
                    viewAllBorrowingRecords();
                    break;
                case 4:
                    viewBooksBorrowedByUser();
                    break;
                case 5:
                    viewOverdueBooks();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Handles the process of a user borrowing a book.
     */
    private static void borrowBook() {
        System.out.println("\n--- Borrow a Book ---");
        String userEmail = getUserStringInput("Enter User Email: ", false);
        if (userEmail == null) return;
        String bookIsbn = getUserStringInput("Enter Book ISBN: ", false);
        if (bookIsbn == null) return;
        int loanDays = getUserIntInput("Enter Loan Duration (days, e.g., 14): ");
        if (loanDays <= 0) {
            System.out.println("Loan duration must be a positive number of days.");
            return;
        }
        borrowingService.borrowBook(userEmail, bookIsbn, loanDays);
    }

    /**
     * Handles the process of a user returning a book.
     */
    private static void returnBook() {
        System.out.println("\n--- Return a Book ---");
        String userEmail = getUserStringInput("Enter User Email who is returning: ", false);
        if (userEmail == null) return;
        String bookIsbn = getUserStringInput("Enter ISBN of the book being returned: ", false);
        if (bookIsbn == null) return;
        borrowingService.returnBook(bookIsbn, userEmail);
    }

    /**
     * Displays all borrowing records in a formatted table.
     */
    private static void viewAllBorrowingRecords() {
        System.out.println("\n--- All Borrowing Records ---");
        List<BorrowingRecord> records = borrowingService.getAllBorrowingRecords();
        if (records.isEmpty()) {
            System.out.println("No borrowing records found.");
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        // Print table header
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-38s | %-40s | %-45s | %-12s | %-12s | %-12s | %-10s\n",
                "RECORD ID", "BOOK TITLE (ISBN)", "USER (NAME - EMAIL)", "BORROW DT.", "DUE DT.", "RETURN DT.", "STATUS");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        // Print each record
        for (BorrowingRecord record : records) {
            Optional<Book> bookOpt = bookService.findBookByIsbn(record.getBookIsbn());
            String bookDisplay = bookOpt.map(b -> b.getTitle() + " (" + b.getIsbn() + ")")
                    .orElse("ISBN: " + record.getBookIsbn() + " (Book Not Found)");

            Optional<User> userOpt = userService.findUserById(record.getUserId());
            String userDisplay = userOpt.map(u -> u.getFirstName() + " " + u.getLastName() + " (" + u.getEmail() + ")")
                    .orElse("User ID: " + record.getUserId() + " (User Not Found)");

            System.out.printf("%-38s | %-40.40s | %-45.45s | %-12s | %-12s | %-12s | %-10s\n",
                    record.getRecordId(),
                    bookDisplay,
                    userDisplay,
                    record.getBorrowDate() != null ? record.getBorrowDate().format(dtf) : "N/A",
                    record.getDueDate() != null ? record.getDueDate().format(dtf) : "N/A",
                    record.getReturnDate() != null ? record.getReturnDate().format(dtf) : "N/A",
                    record.getStatus());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * Displays all books currently borrowed by a specific user.
     */
    private static void viewBooksBorrowedByUser() {
        System.out.println("\n--- Books Currently Borrowed by User ---");
        String userEmail = getUserStringInput("Enter User Email (cannot be empty): ", false);
        if (userEmail == null) return;
        Optional<User> userOpt = userService.findUserByEmail(userEmail);

        if (userOpt.isEmpty()) {
            System.out.println("User with email " + userEmail + " not found.");
            return;
        }

        User user = userOpt.get();
        List<BorrowingRecord> records = borrowingService.getBorrowedBooksByUser(user.getUserId());

        if (records.isEmpty()) {
            System.out.println("User " + user.getFirstName() + " " + user.getLastName() + " (Email: " + userEmail + ") has no books currently borrowed.");
            return;
        }

        System.out.println("Books currently borrowed by " + user.getFirstName() + " " + user.getLastName() + " (Email: " + userEmail + "):");
        // Print table header
        System.out.println("----------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-38s | %-30s | %-20s | %-12s | %-12s\n",
                "RECORD ID", "BOOK TITLE", "BOOK ISBN", "BORROW DATE", "DUE DATE");
        System.out.println("----------------------------------------------------------------------------------------------------------------------");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        // Print each borrowed book's record
        for (BorrowingRecord record : records) {
            Optional<Book> bookOpt = bookService.findBookByIsbn(record.getBookIsbn());
            String bookTitle = bookOpt.map(Book::getTitle).orElse("Title Not Found");

            System.out.printf("%-38s | %-30.30s | %-20s | %-12s | %-12s\n",
                    record.getRecordId(),
                    bookTitle,
                    record.getBookIsbn(),
                    record.getBorrowDate() != null ? record.getBorrowDate().format(dtf) : "N/A",
                    record.getDueDate() != null ? record.getDueDate().format(dtf) : "N/A");
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * Displays all books that are currently overdue.
     */
    private static void viewOverdueBooks() {
        System.out.println("\n--- Overdue Books ---");
        List<BorrowingRecord> records = borrowingService.getOverdueBooks();
        if (records.isEmpty()) {
            System.out.println("No overdue books currently.");
            return;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        System.out.println("The following books are overdue:");
        // Print table header
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-40s | %-45s | %-12s | %-12s | %-10s\n",
                "BOOK TITLE (ISBN)", "USER (NAME - EMAIL)", "BORROW DT.", "DUE DT.", "STATUS");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        // Print each overdue record
        for (BorrowingRecord record : records) {
            Optional<Book> bookOpt = bookService.findBookByIsbn(record.getBookIsbn());
            String bookDisplay = bookOpt.map(b -> b.getTitle() + " (" + b.getIsbn() + ")")
                    .orElse("ISBN: " + record.getBookIsbn() + " (Book Not Found)");

            Optional<User> userOpt = userService.findUserById(record.getUserId());
            String userDisplay = userOpt.map(u -> u.getFirstName() + " " + u.getLastName() + " (" + u.getEmail() + ")")
                    .orElse("User ID: " + record.getUserId() + " (User Not Found)");

            System.out.printf("%-40.40s | %-45.45s | %-12s | %-12s | %-10s\n",
                    bookDisplay,
                    userDisplay,
                    record.getBorrowDate() != null ? record.getBorrowDate().format(dtf) : "N/A",
                    record.getDueDate() != null ? record.getDueDate().format(dtf) : "N/A",
                    record.getStatus());
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
    }


    // --- Utility Methods for Input ---

    /**
     * Gets a string input from the user.
     * If allowEmpty is false, it will keep prompting until a non-empty string is entered.
     * If allowEmpty is true, an empty string is accepted.
     *
     * @param prompt     The message to display to the user.
     * @param allowEmpty True if an empty input is acceptable, false otherwise.
     * @return The trimmed string input from the user, or null if allowEmpty is false and user provides no valid input (can be enhanced for cancellation).
     */
    private static String getUserStringInput(String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim(); // Trim whitespace from input
            if (!input.isEmpty()) {
                return input; // Return non-empty input
            }
            // If input is empty
            if (allowEmpty) {
                return ""; // Return empty string if allowed
            }
            // If empty input is not allowed
            System.out.println("This field cannot be empty. Please try again.");
            // Optionally, add a way to "cancel" input, e.g., by typing "cancel"
            // if (input.equalsIgnoreCase("cancel")) return null; // Example cancellation
        }
    }

    /**
     * Overloaded method for getUserStringInput where empty input is not allowed by default.
     *
     * @param prompt The message to display to the user.
     * @return The non-empty trimmed string input from the user.
     */
    private static String getUserStringInput(String prompt) {
        return getUserStringInput(prompt, false); // Default to not allowing empty
    }

    /**
     * Gets a string input from the user for an update operation.
     * Allows the user to press Enter to keep the default value or type '*' to clear an optional field.
     *
     * @param prompt         The message to display to the user.
     * @param defaultValue   The current value of the field.
     * @param allowMakeEmpty True if the field can be cleared using '*', false otherwise.
     * @return The updated string, the default value, or an empty string if cleared.
     */
    private static String getUserStringInputWithDefaultOrClear(String prompt, String defaultValue, boolean allowMakeEmpty) {
        String displayDefault = (defaultValue == null || defaultValue.isEmpty()) ? "N/A" : defaultValue;
        System.out.print(prompt + "(current: " + displayDefault + ", Enter to keep, '*' to clear if allowed): ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return defaultValue; // User pressed Enter, keep default
        }
        if (allowMakeEmpty && input.equals("*")) {
            return ""; // User wants to clear the optional field
        }
        // If it's a mandatory field (allowMakeEmpty = false) and user tries to clear with '*'
        if (!allowMakeEmpty && input.equals("*")) {
            System.out.println("This field is mandatory and cannot be cleared with '*'. Keeping current value.");
            return defaultValue;
        }
        return input; // User entered a new value
    }


    /**
     * Gets an integer input from the user.
     * Keeps prompting until a valid whole number is entered.
     *
     * @param prompt The message to display to the user.
     * @return The integer input from the user.
     */
    private static int getUserIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int input = Integer.parseInt(scanner.nextLine().trim()); // Trim input before parsing
                return input;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    /**
     * Gets an integer input from the user for an update operation.
     * Allows the user to press Enter to keep the default value.
     *
     * @param prompt       The message to display to the user.
     * @param defaultValue The current value of the field.
     * @return The updated integer or the default value.
     */
    private static int getUserIntInputWithDefault(String prompt, int defaultValue) {
        while (true) {
            System.out.print(prompt + "(current: " + defaultValue + ", press Enter to keep): ");
            String inputStr = scanner.nextLine().trim();
            if (inputStr.isEmpty()) {
                return defaultValue; // User pressed Enter, keep default
            }
            try {
                return Integer.parseInt(inputStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number or press Enter to keep current value.");
            }
        }
    }

    /**
     * Validates an email string against a simple regex pattern.
     *
     * @param email The email string to validate.
     * @return True if the email format is considered valid, false otherwise.
     */
    private static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false; // Null or empty email is invalid
        }
        // A common, relatively simple regex for email validation.
        // It checks for: something@something.something
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}