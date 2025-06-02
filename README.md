# Library Management System - Console Edition

A console-based application developed in Java to manage a small library's operations. This project serves as a practical exercise for core Java programming, Object-Oriented principles, data persistence using JSON, and service-oriented design.

## Features

The system currently supports the following functionalities:

**1. Book Management:**
   - **Add New Book:** Add new books to the library catalog with details like ISBN, title, author, publisher, page count, category, stock, and description.
   - **View All Books:** Display a formatted list of all books available in the library.
   - **Find Book by ISBN:** Search for a specific book using its unique ISBN.
   - **Find Books by Title:** Search for books whose titles contain a given keyword.
   - **Update Book:** Modify the details of an existing book.
   - **Delete Book:** Remove a book from the catalog (with a warning if the book is currently borrowed).

**2. User (Member) Management:**
   - **Add New User:** Register new library members with their personal and contact information.
   - **View All Users:** Display a list of all registered members.
   - **Find User by Email:** Search for a specific user using their email address.
   - **Update User:** Modify the details of an existing member.
   - **Delete User:** Remove a member from the system (with a warning if the user has active borrowings).

**3. Borrowing Management:**
   - **Borrow a Book:** Allow a registered user to borrow an available book for a specified number of days.
   - **Return a Book:** Process the return of a borrowed book.
   - **View All Borrowing Records:** Display a list of all borrowing transactions (past and present).
   - **View Books Borrowed by User:** Show all books currently borrowed by a specific user.
   - **View Overdue Books:** List all books that have passed their due date and have not yet been returned.

## Technologies Used

*   **Java:** Core programming language (Targeting Java 17+ or as per your JDK).
*   **Gradle:** Build automation and dependency management.
*   **Jackson Library:** For JSON serialization and deserialization (reading from and writing to `.json` files).
    *   `jackson-databind`
    *   `jackson-datatype-jsr310` (for Java 8+ Date/Time API support)
*   **Object-Oriented Programming (OOP):** Core design principles applied (Encapsulation, Abstraction through service layers).
*   **Console Interface:** User interaction via standard input/output.

## Project Structure

The project follows a standard Gradle Java project structure:

.
├── data/ # Directory for JSON data files (books.json, users.json, borrowings.json)
├── gradle/
│ └── wrapper/
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── org/example/
│ │ │ ├── Main.java # Main application class with console UI
│ │ │ ├── model/ # Data model classes (POJOs)
│ │ │ │ ├── Book.java
│ │ │ │ ├── User.java
│ │ │ │ ├── BorrowingRecord.java
│ │ │ │ └── enums/
│ │ │ │ ├── BorrowingStatus.java
│ │ │ │ └── MembershipStatus.java
│ │ │ ├── service/ # Service layer classes for business logic
│ │ │ │ ├── BookService.java
│ │ │ │ ├── UserService.java
│ │ │ │ └── BorrowingService.java
│ │ │ └── util/ # Utility classes
│ │ │ └── JsonUtil.java
│ │ └── resources/ # (Currently unused, for non-Java resources)
│ └── test/ # (For unit/integration tests - not implemented in this phase)
│ └── java/
├── build.gradle # Gradle build script
├── gradlew # Gradle wrapper script (Linux/macOS)
├── gradlew.bat # Gradle wrapper script (Windows)
└── settings.gradle # Gradle settings file
## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 17 or higher (or your project's target JDK version).
*   Gradle (optional, as the project includes a Gradle wrapper).

### Running the Application

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/your-repository-name.git
    cd your-repository-name
    ```

2.  **Build and Run using Gradle Wrapper:**
    *   **To build the project:**
        ```bash
        ./gradlew build
        ```
        (On Windows, use `gradlew.bat build`)
    *   **To run the application:**
        ```bash
        ./gradlew run
        ```
        (On Windows, use `gradlew.bat run`)

3.  **Running from an IDE (e.g., IntelliJ IDEA):**
    *   Import the project as a Gradle project.
    *   Locate the `Main.java` file in `src/main/java/org/example/`.
    *   Right-click on `Main.java` and select "Run 'Main.main()'".

### Data Storage

*   Application data (books, users, borrowing records) is stored in JSON files located in the `data/` directory at the root of the project.
*   If the `data/` directory or the JSON files do not exist upon first run, they will be created automatically (initially as empty lists).

## How to Use

Once the application is running, you will be presented with a main menu:
Use code with caution.
|--- Cagan's Library - Main Menu ---|
1- Manage Books
2- Manage Users
3- Manage Borrowings
0- Exit Application
Enter your choice (0-3):
Enter the number corresponding to the desired action and follow the on-screen prompts. Each sub-menu will provide further options for specific operations.

## Future Enhancements (Possible Next Steps)

This project is a foundational Phase 1. Future enhancements could include:

*   **Phase 2: Database Integration:**
    *   Replace JSON file storage with a relational database (e.g., H2, PostgreSQL, MySQL) using JDBC.
    *   Implement a proper Data Access Layer (DAL) with Repository pattern.
*   **Phase 3: Advanced Features & UI:**
    *   **User Interface:** Develop a Graphical User Interface (GUI) using JavaFX or Swing, or a Web Interface using Spring Boot.
    *   **ORM:** Integrate an Object-Relational Mapping tool like Hibernate/JPA.
    *   **Testing:** Add comprehensive unit and integration tests using JUnit/TestNG.
    *   **Logging:** Implement robust logging using SLF4J and Logback/Log4j2.
    *   **Dependency Injection:** Utilize a framework like Spring for managing dependencies.
    *   **Error Handling:** More sophisticated error handling and user feedback.
    *   **Reporting:** More advanced reporting features.
    *   **User Authentication & Authorization:** If developed into a multi-user or web application.

## Contribution

This is primarily a learning project. However, suggestions for improvements or feature ideas are welcome. Please feel free to fork the repository or open an issue.

---
