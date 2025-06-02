package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference; // For generic type handling with Jackson
import org.example.model.User;                       // User data model
import org.example.util.JsonUtil;                    // Utility for JSON operations

import java.io.IOException;                         // For handling file I/O exceptions
import java.time.LocalDate;                         // For setting registration date
import java.util.ArrayList;                       // For creating new lists
import java.util.List;                            // For using List collections
import java.util.Optional;                        // For handling potentially absent values
import java.util.UUID;                            // For generating unique user IDs

/**
 * Service class for managing user-related operations.
 * This includes loading users from a JSON file, saving them,
 * adding, finding, updating, and deleting users.
 */
public class UserService {
    // Path to the JSON file where user data is stored.
    private static final String USERS_FILE_PATH = "data/users.json";
    // In-memory list to hold the users. This list is synchronized with the JSON file.
    private List<User> users;

    /**
     * Constructor for UserService.
     * Initializes the service by loading existing users from the JSON file.
     */
    public UserService() {
        this.users = loadUsersFromFile(); // Load users from file during service instantiation
    }

    /**
     * Loads the list of users from the JSON file specified by USERS_FILE_PATH.
     * This method is typically called once during service initialization.
     *
     * @return A list of User objects. Returns an empty list if the file doesn't exist,
     * is empty, or an error occurs during loading.
     */
    private List<User> loadUsersFromFile() { // Renamed for clarity to distinguish from getAllUsers
        try {
            // Read the list of users using a TypeReference to handle the generic List<User>
            return JsonUtil.readListFromJsonFile(USERS_FILE_PATH, new TypeReference<List<User>>() {
            });
        } catch (IOException e) {
            // Print an error message if loading fails
            System.err.println("Error while loading users from file: " + e.getMessage());
            return new ArrayList<>(); // Return an empty list to prevent NullPointerExceptions
        }
    }

    /**
     * Saves the current in-memory list of users to the JSON file.
     * This method is typically called after any modification to the users list.
     */
    private void saveUsers() { // Changed to private as it's an internal utility
        try {
            JsonUtil.writeListToJsonFile(this.users, USERS_FILE_PATH);
        } catch (IOException e) {
            // Print an error message if saving fails
            System.err.println("Error while saving users to file: " + e.getMessage());
        }
    }

    /**
     * Adds a new user to the system.
     * Checks for email uniqueness. Assigns a unique ID if not provided and sets the registration date.
     * Saves the updated list of users to the JSON file.
     *
     * @param user The User object to be added.
     */
    public void addUser(User user) {
        // Check if a user with the same email already exists (case-insensitive)
        // Ensure users list is not null before streaming (though constructor should prevent this)
        if (this.users != null && this.users.stream().anyMatch(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(user.getEmail()))) {
            System.out.println("Error: User with email " + user.getEmail() + " already exists.");
            return; // Do not add if duplicate email
        }

        // Assign a unique ID if one is not already provided or is empty
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            user.setUserId(UUID.randomUUID().toString());
        }
        // Set the registration date to the current moment
        // This is also done in User constructor, but can be re-set here for consistency if user object was created elsewhere.
        if (user.getRegistrationDate() == null) { // Only set if not already set
            user.setRegistrationDate(LocalDate.now());
        }

        this.users.add(user); // Add the new user to the in-memory list
        saveUsers();          // Persist the changes to the JSON file
        System.out.println("User added successfully: " + user.getFirstName() + " " + user.getLastName());
    }

    /**
     * Retrieves a copy of all registered users.
     *
     * @return A new list containing all User objects. Modifying this returned list
     * will not affect the original list in the service.
     */
    public List<User> getAllUsers() {
        // Return a copy to prevent external modification of the internal list
        // Ensure 'users' list is not null, though it should be initialized in constructor
        return this.users != null ? new ArrayList<>(this.users) : new ArrayList<>();
    }

    // The method `getAlUsers` was likely a typo for `getAllUsers`.
    // If it was intentional for another purpose, it should be documented.
    // Assuming it's a typo and `getAllUsers()` is the intended public method.
    /*
    public List<User> getAlUsers() { // This seems like a typo of getAllUsers()
        return new ArrayList<>(this.users);
    }
    */


    /**
     * Finds a user by their unique ID (case-insensitive).
     *
     * @param userId The ID of the user to find.
     * @return An Optional containing the User if found, otherwise an empty Optional.
     */
    public Optional<User> findUserById(String userId) {
        if (userId == null || this.users == null) return Optional.empty();
        return this.users.stream()
                .filter(user -> user.getUserId() != null && user.getUserId().equalsIgnoreCase(userId))
                .findFirst();
    }

    /**
     * Finds a user by their email address (case-insensitive).
     * Email is assumed to be a unique identifier for users.
     *
     * @param email The email address of the user to find.
     * @return An Optional containing the User if found, otherwise an empty Optional.
     */
    public Optional<User> findUserByEmail(String email) {
        if (email == null || this.users == null) return Optional.empty();
        return this.users.stream()
                .filter(user -> user.getEmail() != null && user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    /**
     * Updates the details of an existing user.
     * The user to be updated is identified by their ID from the {@code updatedUser} object.
     * Checks for email uniqueness if the email is being changed.
     *
     * @param updatedUser The User object containing the new details. The UserID must match an existing user.
     * @return True if the update was successful, false otherwise (e.g., user not found, or new email conflicts).
     */
    public boolean updateUser(User updatedUser) {
        if (updatedUser == null || updatedUser.getUserId() == null) {
            System.out.println("Error: Updated user or user ID cannot be null.");
            return false;
        }
        Optional<User> existingUserOpt = findUserById(updatedUser.getUserId());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            // Check for email conflict if email is being changed
            if (updatedUser.getEmail() != null && !existingUser.getEmail().equalsIgnoreCase(updatedUser.getEmail())) {
                // Check if the new email is already used by ANOTHER user
                if (this.users.stream().anyMatch(u -> u.getEmail() != null &&
                        u.getEmail().equalsIgnoreCase(updatedUser.getEmail()) &&
                        !u.getUserId().equals(existingUser.getUserId()))) { // Exclude the current user from check
                    System.out.println("Error: Another user with email " + updatedUser.getEmail() + " already exists.");
                    return false;
                }
            }

            // Update fields of the existing user
            existingUser.setFirstName(updatedUser.getFirstName());
            existingUser.setLastName(updatedUser.getLastName());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            existingUser.setEmail(updatedUser.getEmail()); // Update email after conflict check
            existingUser.setAddress(updatedUser.getAddress());
            existingUser.setStatus(updatedUser.getStatus());
            // Registration date is typically not updated.

            saveUsers(); // Persist the changes
            System.out.println("User updated: " + existingUser.getFirstName() + " " + existingUser.getLastName());
            return true;
        }
        System.out.println("Error: User with ID " + updatedUser.getUserId() + " not found for update.");
        return false;
    }

    /**
     * Deletes a user from the system based on their ID.
     *
     * @param userId The ID of the user to be deleted.
     * @return True if the user was found and deleted, false otherwise.
     */
    public boolean deleteUserById(String userId) {
        if (userId == null || this.users == null) return false;
        // Remove the user from the in-memory list if their ID matches (case-insensitive)
        boolean removed = this.users.removeIf(user -> user.getUserId() != null && user.getUserId().equalsIgnoreCase(userId));
        if (removed) {
            saveUsers(); // Persist the changes if a user was removed
            System.out.println("User with ID " + userId + " deleted successfully.");
        } else {
            System.out.println("Error: User with ID " + userId + " not found for deletion.");
        }
        return removed; // Return the status of the removal operation
    }
}