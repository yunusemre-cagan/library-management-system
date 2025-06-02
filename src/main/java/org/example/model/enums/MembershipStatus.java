package org.example.model.enums;

/**
 * Represents the possible statuses of a user's library membership.
 * This enum is used to track the current state of a user's account,
 * such as whether it's active, inactive, suspended, or expired.
 */
public enum MembershipStatus {
    /**
     * Indicates that the user's membership is currently active and they can use library services.
     */
    ACTIVE,

    /**
     * Indicates that the user's membership is currently inactive or passive.
     * This might mean the user has voluntarily paused their membership or it's a default state
     * before full activation or after a period of inactivity. Library services might be restricted.
     */
    PASSIVE,

    /**
     * Indicates that the user's membership has been temporarily suspended due to rule violations
     * or other administrative reasons. Library services are typically unavailable during suspension.
     */
    SUSPENDED,

    /**
     * Indicates that the user's membership period has ended and needs to be renewed.
     * Library services are typically unavailable until renewal.
     */
    EXPIRED
}