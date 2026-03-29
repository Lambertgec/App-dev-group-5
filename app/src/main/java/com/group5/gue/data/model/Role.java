package com.group5.gue.data.model;

/**
 * Defines the access levels for users within the application.
 *
 */
public enum Role {
    /**
     * Represents a standard student user.
     */
    USER,

    /** 
     * Represents an administrative user.
     * Administrators have elevated privileges to manage lecture rooms, 
     * generate verification codes, and view global analytics.
     */
    ADMIN
}